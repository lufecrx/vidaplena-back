package ifba.engsoft.vidaplena.domain.service.familia;

import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaResponseDTO;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.familia.VinculoDependencia;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.VinculoDependenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Service
public class VinculoDependenciaService {

    private static final int IDADE_MAIORIDADE = 18;

    @Autowired
    private VinculoDependenciaRepository vinculoDependenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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
}
