package ifba.engsoft.vidaplena.domain.service.familia;

import ifba.engsoft.vidaplena.domain.dto.familia.CadastroDependenteRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.DependenteResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaResponseDTO;
import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.familia.TipoDependencia;
import ifba.engsoft.vidaplena.domain.model.familia.VinculoDependencia;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.VinculoDependenciaRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class VinculoDependenciaService {

    private static final int IDADE_MAIORIDADE = 18;

    @Autowired
    private VinculoDependenciaRepository vinculoDependenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    /**
     * Cria um novo vínculo de dependência aplicando todas as regras de negócio:
     * 1. Impedir auto-dependência (usuário não pode ser dependente de si mesmo)
     * 2. Validar que o responsável possui maioridade legal (>= 18 anos)
     * 3. Impedir vínculos ativos duplicados entre os mesmos usuários
     * 4. Validar data de início não futura
     * 
     * @param dto DTO contendo os dados do vínculo a ser criado
     * @return DTO de resposta com os dados do vínculo criado
     * @throws RegraNegocioException se alguma regra de negócio for violada
     */
    @Transactional
    public VinculoDependenciaResponseDTO criarVinculo(VinculoDependenciaRequestDTO dto) {
        // Um usuário não pode ser dependente de si mesmo.
        if (dto.responsavelId().equals(dto.dependenteId())) {
            throw new RegraNegocioException(
                    "Um usuário não pode ser dependente de si mesmo. " +
                    "Responsável e dependente devem ser usuários distintos."
            );
        }

    // Vinculos só podem ter data de início no passado ou hoje.
    if (dto.dataInicio() != null && dto.dataInicio().isAfter(LocalDate.now())) {
            throw new RegraNegocioException(
                    "A data de início do vínculo não pode ser uma data futura. " +
                    "Data informada: " + dto.dataInicio() + ". Data atual: " + LocalDate.now()
            );
        }

        // O responsável deve existir no sistema e ter idade >= 18 anos.
        Usuario responsavel = usuarioRepository.findById(dto.responsavelId())
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário responsável com ID " + dto.responsavelId() + " não encontrado no sistema."
                ));

        validarMaioridadeDoResponsavel(responsavel);

        Usuario dependente = usuarioRepository.findById(dto.dependenteId())
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário dependente com ID " + dto.dependenteId() + " não encontrado no sistema."
                ));

        validarUnicidadeDeVinculoAtivo(dto.responsavelId(), dto.dependenteId());

        // Todas as validações passaram. Criamos o objeto e persistimos.
        VinculoDependencia vinculo = new VinculoDependencia();
        vinculo.setTipo(dto.tipo());
        vinculo.setDataInicio(dto.dataInicio());
        vinculo.setResponsavel(responsavel);
        vinculo.setDependente(dependente);

        VinculoDependencia vinculoSalvo = vinculoDependenciaRepository.save(vinculo);

        return mapearParaResponseDTO(vinculoSalvo);
    }

    /**
     * Soft delete (inativação) de um vínculo de dependência. Marca a dataFim como a data atual.
     * 
     * @param vinculoId ID do vínculo a ser inativado
     * @return DTO de resposta com os dados do vínculo inativado
     * @throws RegraNegocioException se o vínculo não existir ou já estiver inativo
     */
    @Transactional
    public VinculoDependenciaResponseDTO inativarVinculo(UUID vinculoId) {
        VinculoDependencia vinculo = vinculoDependenciaRepository.findById(vinculoId)
                .orElseThrow(() -> new RegraNegocioException(
                        "Vínculo de dependência com ID " + vinculoId + " não encontrado."
                ));

        if (vinculo.getDataFim() != null) {
            throw new RegraNegocioException(
                    "O vínculo com ID " + vinculoId + " já está inativado. Data de fim: " + vinculo.getDataFim()
            );
        }

        vinculo.setDataFim(LocalDate.now());
        VinculoDependencia vinculoInativado = vinculoDependenciaRepository.save(vinculo);

        return mapearParaResponseDTO(vinculoInativado);
    }

    @Transactional(readOnly = true)
    public VinculoDependenciaResponseDTO obterPorId(UUID id) {
        VinculoDependencia vinculo = vinculoDependenciaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Vínculo de dependência com ID " + id + " não encontrado."));
        return mapearParaResponseDTO(vinculo);
    }

    @Transactional(readOnly = true)
    public java.util.List<VinculoDependenciaResponseDTO> listarPorResponsavel(UUID responsavelId, boolean apenasAtivos) {
        java.util.List<VinculoDependencia> vinculos = apenasAtivos
                ? vinculoDependenciaRepository.findByResponsavelIdAndDataFimIsNull(responsavelId)
                : vinculoDependenciaRepository.findByResponsavelId(responsavelId);

        return vinculos.stream()
                .map(this::mapearParaResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<VinculoDependenciaResponseDTO> listarPorDependente(UUID dependenteId, boolean apenasAtivos) {
        java.util.List<VinculoDependencia> vinculos = apenasAtivos
                ? vinculoDependenciaRepository.findByDependenteIdAndDataFimIsNull(dependenteId)
                : vinculoDependenciaRepository.findByDependenteId(dependenteId);

        return vinculos.stream()
                .map(this::mapearParaResponseDTO)
                .toList();
    }


    /**
     * Valida se o responsável possui idade legal/maioridade com base na data
     * de nascimento. 
     *
     * @param responsavel o usuário que será o responsável pelo vínculo
     * @throws RegraNegocioException se o responsável for menor de 18 anos
     */
    private void validarMaioridadeDoResponsavel(Usuario responsavel) {
        if (responsavel.getDataNascimento() == null) {
            throw new RegraNegocioException(
                    "O usuário responsável (ID: " + responsavel.getId() + ", nome: " + responsavel.getNome() +
                    ") não possui data de nascimento cadastrada. Cadastre a data antes de criar o vínculo."
            );
        }

        int idadeResponsavel = Period.between(responsavel.getDataNascimento(), LocalDate.now()).getYears();

        if (idadeResponsavel < IDADE_MAIORIDADE) {
            throw new RegraNegocioException(
                    "O responsável deve ter idade legal (maior de " + IDADE_MAIORIDADE + " anos). " +
                    "Usuário: " + responsavel.getNome() + " (ID: " + responsavel.getId() + "). " +
                    "Idade atual: " + idadeResponsavel + " anos. " +
                    "Data de nascimento: " + responsavel.getDataNascimento()
            );
        }
    }

    /**
     * Valida a unicidade de vínculos ativos entre os mesmos dois usuários.
     * Se já existir um vínculo ativo (dataFim IS NULL) entre o mesmo responsável
     * e o mesmo dependente, uma exceção é lançada.
     *
     * @param responsavelId ID do usuário responsável
     * @param dependenteId  ID do usuário dependente
     * @throws RegraNegocioException se já existir vínculo ativo entre estes usuários
     */
    private void validarUnicidadeDeVinculoAtivo(UUID responsavelId, UUID dependenteId) {
        VinculoDependencia vinculoExistente = vinculoDependenciaRepository
                .findByResponsavelIdAndDependenteIdAndDataFimIsNull(responsavelId, dependenteId);

        if (vinculoExistente != null) {
            throw new RegraNegocioException(
                    "Já existe um vínculo ativo entre o responsável (ID: " + responsavelId + ") " +
                    "e o dependente (ID: " + dependenteId + "). " +
                    "Vínculo existente ID: " + vinculoExistente.getId() +
                    ", tipo: " + vinculoExistente.getTipo() +
                    ", data início: " + vinculoExistente.getDataInicio() +
                    ". Não é possível criar um novo vínculo ativo."
            );
        }
    }

    /**
     * Mapeia um objeto VinculoDependencia para o DTO de resposta.
     * 
     * @param vinculo o objeto VinculoDependencia a ser mapeado
     * @return DTO de resposta contendo os dados do vínculo
     */
    private VinculoDependenciaResponseDTO mapearParaResponseDTO(VinculoDependencia vinculo) {
        return new VinculoDependenciaResponseDTO(
                vinculo.getId(),
                vinculo.getResponsavel().getNome(),
                vinculo.getDependente().getNome(),
                vinculo.getTipo(),
                vinculo.getDataInicio(),
                vinculo.getDataFim()
        );
    }

    /**
     * Cadastro unificado e atômico de dependente (criança, idoso, PcD) vinculado ao perfil do responsável.
     * Na mesma transação:
     * 1. Valida permissões e maioridade do responsável
     * 2. Atribui a role RESPONSAVEL para o usuário caso ainda não possua
     * 3. Valida dados do dependente (regras por TipoDependencia: idade, CPF, datas)
     * 4. Cria ou localiza a conta Usuario do dependente
     * 5. Cria ou atualiza o perfil clínico Paciente do dependente
     * 6. Cria o VinculoDependencia
     *
     * @param dto dados consolidados para o cadastro do dependente
     * @param emailAutenticado e-mail do usuário autenticado no token JWT
     * @return DTO consolidado com informações do vínculo, usuário e paciente
     */
    @Transactional
    public DependenteResponseDTO cadastrarDependente(CadastroDependenteRequestDTO dto, String emailAutenticado) {
        if (emailAutenticado == null || emailAutenticado.isBlank()) {
            throw new RegraNegocioException("Usuário não autenticado no sistema.");
        }

        Usuario usuarioAutenticado = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(emailAutenticado))
                .orElseThrow(() -> new RegraNegocioException("Usuário autenticado não encontrado no sistema."));

        // Resolver o responsável: o próprio usuário autenticado ou outro especificado por um ADMINISTRADOR
        Usuario responsavel;
        if (dto.responsavelId() != null) {
            boolean isAdmin = usuarioAutenticado.possuiTipo(TipoUsuario.ADMINISTRADOR);
            if (!isAdmin && !dto.responsavelId().equals(usuarioAutenticado.getId())) {
                throw new RegraNegocioException("Apenas administradores podem cadastrar dependentes para outro responsável.");
            }
            responsavel = usuarioRepository.findById(dto.responsavelId())
                    .orElseThrow(() -> new RegraNegocioException("Usuário responsável com ID " + dto.responsavelId() + " não encontrado no sistema."));
        } else {
            responsavel = usuarioAutenticado;
        }

        // Validação da maioridade do responsável (>= 18 anos)
        validarMaioridadeDoResponsavel(responsavel);

        // Garantir que o responsável possua a role RESPONSAVEL
        if (!responsavel.possuiTipo(TipoUsuario.RESPONSAVEL)) {
            responsavel.getTipos().add(TipoUsuario.RESPONSAVEL);
            responsavel = usuarioRepository.save(responsavel);
        }

        // Validações do Dependente
        String cpfLimpo = normalizarCpf(dto.cpf());
        if (cpfLimpo == null || cpfLimpo.isBlank()) {
            throw new RegraNegocioException("O CPF do dependente é obrigatório.");
        }
        if (cpfLimpo.length() != 11) {
            throw new RegraNegocioException("O CPF informado deve conter exatamente 11 dígitos numéricos.");
        }

        // Bloqueio de auto-dependência
        String responsavelCpfLimpo = normalizarCpf(responsavel.getCpf());
        if (cpfLimpo.equals(responsavelCpfLimpo)) {
            throw new RegraNegocioException("Um usuário não pode ser dependente de si mesmo. O CPF informado pertence ao próprio responsável.");
        }

        // Validação de data de nascimento
        if (dto.dataNascimento() == null || dto.dataNascimento().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data de nascimento do dependente é obrigatória e não pode ser futura.");
        }

        int idadeDependente = Period.between(dto.dataNascimento(), LocalDate.now()).getYears();

        // Validação de coerência da faixa etária com o tipo de dependência
        if (dto.tipo() == TipoDependencia.CRIANCA && idadeDependente >= 18) {
            throw new RegraNegocioException(
                    "A classificação 'CRIANCA' é restrita a menores de 18 anos. Idade calculada: " + idadeDependente + " anos."
            );
        }
        if (dto.tipo() == TipoDependencia.IDOSO && idadeDependente < 60) {
            throw new RegraNegocioException(
                    "A classificação 'IDOSO' é restrita a pessoas com 60 anos ou mais. Idade calculada: " + idadeDependente + " anos."
            );
        }

        // Validações temporais do vínculo
        LocalDate dataInicio = dto.dataInicio() != null ? dto.dataInicio() : LocalDate.now();
        if (dataInicio.isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data de início do vínculo não pode ser futura. Data informada: " + dataInicio);
        }
        if (dto.dataFim() != null && dto.dataFim().isBefore(dataInicio)) {
            throw new RegraNegocioException("A data de fim do vínculo não pode ser anterior à data de início.");
        }

        // Obter ou cadastrar a conta Usuario do dependente
        Optional<Usuario> usuarioExistenteOpt = usuarioRepository.findByCpf(cpfLimpo);
        Usuario dependente;

        if (usuarioExistenteOpt.isPresent()) {
            dependente = usuarioExistenteOpt.get();
            if (dependente.getId().equals(responsavel.getId())) {
                throw new RegraNegocioException("Auto-dependência não permitida. O usuário já cadastrado é o próprio responsável.");
            }
            // Verificar unicidade de vínculo ativo
            validarUnicidadeDeVinculoAtivo(responsavel.getId(), dependente.getId());
        } else {
            // Resolver e-mail: se não informado, gera um e-mail interno seguro
            String emailFinal;
            if (dto.email() != null && !dto.email().isBlank()) {
                emailFinal = normalizarEmail(dto.email());
                if (usuarioRepository.existsByEmailIgnoreCase(emailFinal)) {
                    throw new RegraNegocioException("O e-mail '" + emailFinal + "' já está cadastrado para outro usuário.");
                }
            } else {
                emailFinal = "dep." + cpfLimpo + "@dependente.vidaplena.local";
                if (usuarioRepository.existsByEmailIgnoreCase(emailFinal)) {
                    emailFinal = "dep." + cpfLimpo + "." + UUID.randomUUID().toString().substring(0, 8) + "@dependente.vidaplena.local";
                }
            }

            // Senha segura gerada automaticamente para o dependente
            String senhaPlana = UUID.randomUUID().toString();
            String senhaCodificada = (passwordEncoder != null) ? passwordEncoder.encode(senhaPlana) : senhaPlana;

            String telefone = (dto.telefone() != null && !dto.telefone().isBlank()) ? dto.telefone().trim() : responsavel.getTelefone();

            Usuario novoUsuario = new Usuario(
                    dto.nome().trim(),
                    cpfLimpo,
                    emailFinal,
                    senhaCodificada,
                    telefone,
                    dto.dataNascimento(),
                    StatusUsuario.ATIVO,
                    Set.of(TipoUsuario.PACIENTE)
            );
            dependente = usuarioRepository.save(novoUsuario);
        }

        // Criar ou atualizar perfil clínico Paciente
        Paciente paciente = pacienteRepository.findByUsuario(dependente)
                .orElseGet(() -> new Paciente(dependente));

        if (dto.tipoSanguineo() != null) {
            paciente.setTipoSanguineo(dto.tipoSanguineo());
        }
        if (dto.alergias() != null) {
            paciente.setAlergias(dto.alergias());
        }
        if (dto.medicamentosContinuos() != null) {
            paciente.setMedicamentosContinuos(dto.medicamentosContinuos());
        }
        if (dto.historicoFamiliar() != null && !dto.historicoFamiliar().isBlank()) {
            paciente.setHistoricoFamiliar(dto.historicoFamiliar().trim());
        }
        Paciente pacienteSalvo = pacienteRepository.save(paciente);

        // Criar o VinculoDependencia
        validarUnicidadeDeVinculoAtivo(responsavel.getId(), dependente.getId());
        VinculoDependencia vinculo = new VinculoDependencia();
        vinculo.setResponsavel(responsavel);
        vinculo.setDependente(dependente);
        vinculo.setTipo(dto.tipo());
        vinculo.setDataInicio(dataInicio);
        vinculo.setDataFim(dto.dataFim());

        VinculoDependencia vinculoSalvo = vinculoDependenciaRepository.save(vinculo);

        return mapearParaDependenteResponseDTO(vinculoSalvo, pacienteSalvo);
    }

    @Transactional(readOnly = true)
    public List<DependenteResponseDTO> listarDependentes(String emailAutenticado, UUID responsavelIdFiltro, boolean apenasAtivos) {
        if (emailAutenticado == null || emailAutenticado.isBlank()) {
            throw new RegraNegocioException("Usuário não autenticado no sistema.");
        }

        Usuario usuarioAutenticado = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(emailAutenticado))
                .orElseThrow(() -> new RegraNegocioException("Usuário autenticado não encontrado."));

        UUID responsavelIdAlvo;
        if (responsavelIdFiltro != null) {
            boolean isAdmin = usuarioAutenticado.possuiTipo(TipoUsuario.ADMINISTRADOR);
            if (!isAdmin && !responsavelIdFiltro.equals(usuarioAutenticado.getId())) {
                throw new RegraNegocioException("Acesso negado: você só pode consultar os dependentes do seu próprio perfil.");
            }
            responsavelIdAlvo = responsavelIdFiltro;
        } else {
            responsavelIdAlvo = usuarioAutenticado.getId();
        }

        List<VinculoDependencia> vinculos = apenasAtivos
                ? vinculoDependenciaRepository.findByResponsavelIdAndDataFimIsNull(responsavelIdAlvo)
                : vinculoDependenciaRepository.findByResponsavelId(responsavelIdAlvo);

        return vinculos.stream()
                .map(v -> {
                    Paciente p = pacienteRepository.findByUsuario(v.getDependente()).orElse(null);
                    return mapearParaDependenteResponseDTO(v, p);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public DependenteResponseDTO obterDependente(UUID vinculoId, String emailAutenticado) {
        if (emailAutenticado == null || emailAutenticado.isBlank()) {
            throw new RegraNegocioException("Usuário não autenticado no sistema.");
        }

        Usuario usuarioAutenticado = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(emailAutenticado))
                .orElseThrow(() -> new RegraNegocioException("Usuário autenticado não encontrado."));

        VinculoDependencia vinculo = vinculoDependenciaRepository.findById(vinculoId)
                .orElseThrow(() -> new RegraNegocioException("Vínculo de dependência com ID " + vinculoId + " não encontrado."));

        boolean isAdmin = usuarioAutenticado.possuiTipo(TipoUsuario.ADMINISTRADOR);
        boolean isResponsavel = vinculo.getResponsavel().getId().equals(usuarioAutenticado.getId());
        boolean isDependente = vinculo.getDependente().getId().equals(usuarioAutenticado.getId());

        if (!isAdmin && !isResponsavel && !isDependente) {
            throw new RegraNegocioException("Acesso negado: você não possui permissão para visualizar este vínculo.");
        }

        Paciente paciente = pacienteRepository.findByUsuario(vinculo.getDependente()).orElse(null);
        return mapearParaDependenteResponseDTO(vinculo, paciente);
    }

    private DependenteResponseDTO mapearParaDependenteResponseDTO(VinculoDependencia vinculo, Paciente paciente) {
        Usuario dep = vinculo.getDependente();
        int idade = dep.getDataNascimento() != null ? Period.between(dep.getDataNascimento(), LocalDate.now()).getYears() : 0;

        List<String> alergias = (paciente != null && paciente.getAlergias() != null)
                ? new java.util.ArrayList<>(paciente.getAlergias())
                : List.of();

        List<String> medicamentos = (paciente != null && paciente.getMedicamentosContinuos() != null)
                ? new java.util.ArrayList<>(paciente.getMedicamentosContinuos())
                : List.of();

        return new DependenteResponseDTO(
                vinculo.getId(),
                dep.getId(),
                paciente != null ? paciente.getId() : null,
                vinculo.getResponsavel().getId(),
                vinculo.getResponsavel().getNome(),
                dep.getNome(),
                dep.getCpf(),
                dep.getEmail(),
                dep.getTelefone(),
                dep.getDataNascimento(),
                idade,
                vinculo.getTipo(),
                vinculo.getDataInicio(),
                vinculo.getDataFim(),
                paciente != null ? paciente.getTipoSanguineo() : null,
                alergias,
                medicamentos,
                paciente != null ? paciente.getHistoricoFamiliar() : null
        );
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
