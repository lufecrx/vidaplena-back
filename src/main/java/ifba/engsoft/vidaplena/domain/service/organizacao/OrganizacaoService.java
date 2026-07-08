package ifba.engsoft.vidaplena.domain.service.organizacao;

import ifba.engsoft.vidaplena.domain.dto.organizacao.ClinicaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.ClinicaResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.EmpresaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.EmpresaResponseDTO;
import ifba.engsoft.vidaplena.domain.model.organizacao.Clinica;
import ifba.engsoft.vidaplena.domain.model.organizacao.Empresa;
import ifba.engsoft.vidaplena.domain.model.organizacao.Organizacao;
import ifba.engsoft.vidaplena.domain.repository.organizacao.OrganizacaoRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.VinculoDependenciaRepository;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.ProfissionalRepository;
import ifba.engsoft.vidaplena.domain.model.familia.VinculoDependencia;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.service.familia.RegraNegocioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizacaoService {

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @Autowired
    private VinculoDependenciaRepository vinculoDependenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;
    /**
     * Cria uma nova empresa com o CNPJ e setor informados.
     * Valida se o CNPJ já não está em uso por outra organização.
     * 
     * @param dto DTO contendo os dados da empresa a ser criada
     * @return DTO de resposta com os dados da empresa criada
     * @throws RegraNegocioException se o CNPJ já estiver em uso
     */
    @Transactional
    public EmpresaResponseDTO criarEmpresa(EmpresaRequestDTO dto) {
        // Verificar se o CNPJ já existe
        Optional<Organizacao> organizacaoExistente = organizacaoRepository.findByCnpj(dto.cnpj());
        if (organizacaoExistente.isPresent()) {
            throw new RegraNegocioException(
                    "Já existe uma organização cadastrada com o CNPJ informado: " + dto.cnpj()
            );
        }

        // Criar a empresa
        Empresa empresa = new Empresa(dto.nome(), dto.cnpj(), dto.setor());

        Empresa empresaSalva = (Empresa) organizacaoRepository.save(empresa);

        return new EmpresaResponseDTO(
                empresaSalva.getId(),
                empresaSalva.getNome(),
                empresaSalva.getCnpj(),
                empresaSalva.getSetor()
        );
    }

    /**
     * Cria uma nova clínica com o CNPJ e tipo informados.
     * Valida se o CNPJ já não está em uso por outra organização.
     * 
     * @param dto DTO contendo os dados da clínica a ser criada
     * @return DTO de resposta com os dados da clínica criada
     * @throws RegraNegocioException se o CNPJ já estiver em uso
     */
    @Transactional
    public ClinicaResponseDTO criarClinica(ClinicaRequestDTO dto) {
        // Verificar se o CNPJ já existe
        Optional<Organizacao> organizacaoExistente = organizacaoRepository.findByCnpj(dto.cnpj());
        if (organizacaoExistente.isPresent()) {
            throw new RegraNegocioException(
                    "Já existe uma organização cadastrada com o CNPJ informado: " + dto.cnpj()
            );
        }

        // Criar a clínica
        Clinica clinica = new Clinica(dto.nome(), dto.cnpj(), dto.tipo());

        Clinica clinicaSalva = (Clinica) organizacaoRepository.save(clinica);

        return new ClinicaResponseDTO(
                clinicaSalva.getId(),
                clinicaSalva.getNome(),
                clinicaSalva.getCnpj(),
                clinicaSalva.getTipo()
        );
    }

    /**
     * REGRA DE EXCLUSÃO DE ORGANIZAÇÕES
     * 
     * Impedir a exclusão física de uma Organização (Empresa, Clínica ou qualquer
     * subtipo) que possua membros, funcionários ou dependentes vinculados ativos.
     * 
     * Esta regra garante:
     * 1. Não há perda de dados históricos de vínculo
     * 2. Não há integridade referencial quebrada
     * 3. A auditoria pode rastrear todas as relações passadas
     * 4. Organizações devem ser inativadas logicamente antes da exclusão
     * 
     * @param organizacaoId ID da organização a ser excluída
     * @throws RegraNegocioException se a organização não existir ou se houver vínculos ativos
     */
    @Transactional
    public void excluirOrganizacao(UUID organizacaoId) {
        // Verificar se a organização existe
        Organizacao organizacao = organizacaoRepository.findById(organizacaoId)
                .orElseThrow(() -> new RegraNegocioException(
                        "Organização com ID " + organizacaoId + " não encontrada."
                ));

        // Verificar se existem vínculos de dependência ativos envolvendo esta organização
        // Como Organizacao é um MappedSuperclass, precisamos verificar pelas subclasses
        long vinculosAtivosEmpresa = 0;
        long vinculosAtivosClinica = 0;

        if (organizacao instanceof Empresa) {
            vinculosAtivosEmpresa = contarVinculosAtivosPorOrganizacaoId(organizacaoId);
        } else if (organizacao instanceof Clinica) {
            vinculosAtivosClinica = contarVinculosAtivosPorOrganizacaoId(organizacaoId);
        }

        long totalVinculosAtivos = vinculosAtivosEmpresa + vinculosAtivosClinica;

        if (totalVinculosAtivos > 0) {
            throw new RegraNegocioException(
                    "Não é possível excluir a organização '" + organizacao.getNome() + 
                    "' (ID: " + organizacaoId + ", tipo: " + organizacao.getClass().getSimpleName() + 
                    "). Existem " + totalVinculosAtivos + " vínculo(s) ativo(s) envolvendo esta organização. " +
                    "Por segurança, organizações com vínculos ativos devem ser inativadas logicamente antes da exclusão."
            );
        }

        // Verificar se a organização possui membros/funcionários vinculados
        int membrosVinculados = contarMembrosVinculados(organizacaoId);

        if (membrosVinculados > 0) {
            throw new RegraNegocioException(
                    "Não é possível excluir a organização '" + organizacao.getNome() + 
                    "' (ID: " + organizacaoId + "). Ela possui " + membrosVinculados + 
                    " membro(s)/funcionário(s) vinculado(s). Remova todos os vínculos antes de excluir."
            );
        }

        // Todas as validações passaram, proceder com a exclusão 
        organizacaoRepository.delete(organizacao);
    }

    /**
     * Soft delete (inativação) de uma organização. Marca a organização como inativa, inativa todos os vínculos ativos e remove membros vinculados.
     * 
     * @param organizacaoId ID da organização a ser inativada
     * @return DTO de resposta com os dados da organização inativada
     * @throws RegraNegocioException se a organização não existir
     */
    @Transactional
    public OrganizacaoResponseDTO inativarOrganizacao(UUID organizacaoId) {
        Organizacao organizacao = organizacaoRepository.findById(organizacaoId)
                .orElseThrow(() -> new RegraNegocioException(
                        "Organização com ID " + organizacaoId + " não encontrada."
                ));

        // Inativar todos os vínculos de dependência associados a esta organização
        inativarVinculosDaOrganizacao(organizacaoId);

        // Remover membros/funcionários vinculados
        removerMembrosVinculados(organizacaoId);

        // Renomear para indicar inativação
        organizacao.setNome("[INATIVO] " + organizacao.getNome());

        organizacaoRepository.save(organizacao);

        return new OrganizacaoResponseDTO(
                organizacao.getId(),
                organizacao.getNome(),
                organizacao.getCnpj(),
                organizacao.getClass().getSimpleName()
        );
    }

    @Transactional(readOnly = true)
    public ClinicaResponseDTO buscarClinicaPorId(UUID id) {
        Organizacao org = organizacaoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Clínica com ID " + id + " não encontrada."));
        
        if (!(org instanceof Clinica clinica)) {
            throw new RegraNegocioException("Organização com ID " + id + " não é uma clínica.");
        }

        validarVinculoRepresentante(id, "clínica");

        return new ClinicaResponseDTO(
                clinica.getId(),
                clinica.getNome(),
                clinica.getCnpj(),
                clinica.getTipo()
        );
    }

    @Transactional(readOnly = true)
    public EmpresaResponseDTO buscarEmpresaPorId(UUID id) {
        Organizacao org = organizacaoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Empresa com ID " + id + " não encontrada."));
        
        if (!(org instanceof Empresa empresa)) {
            throw new RegraNegocioException("Organização com ID " + id + " não é uma empresa.");
        }

        validarVinculoRepresentante(id, "empresa");

        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getNome(),
                empresa.getCnpj(),
                empresa.getSetor()
        );
    }

    @Transactional(readOnly = true)
    public List<ClinicaResponseDTO> listarClinicas() {
        return organizacaoRepository.findAll().stream()
                .filter(org -> org instanceof Clinica)
                .map(org -> {
                    Clinica clinica = (Clinica) org;
                    return new ClinicaResponseDTO(
                            clinica.getId(),
                            clinica.getNome(),
                            clinica.getCnpj(),
                            clinica.getTipo()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> listarEmpresas() {
        return organizacaoRepository.findAll().stream()
                .filter(org -> org instanceof Empresa)
                .map(org -> {
                    Empresa empresa = (Empresa) org;
                    return new EmpresaResponseDTO(
                            empresa.getId(),
                            empresa.getNome(),
                            empresa.getCnpj(),
                            empresa.getSetor()
                    );
                })
                .collect(Collectors.toList());
    }

    private void validarVinculoRepresentante(UUID orgId, String tipoOrg) {
        org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
            if (usuario != null) {
                boolean isAdm = usuario.possuiTipo(ifba.engsoft.vidaplena.domain.model.TipoUsuario.ADMINISTRADOR);
                boolean isRepresentante = usuario.possuiTipo(ifba.engsoft.vidaplena.domain.model.TipoUsuario.REPRESENTANTE_EMPRESA);
                
                if (!isAdm && isRepresentante) {
                    if ("clínica".equals(tipoOrg)) {
                        if (usuario.getClinica() == null || !usuario.getClinica().getId().equals(orgId)) {
                            throw new org.springframework.security.access.AccessDeniedException(
                                    "Usuário representante não está vinculado a esta clínica."
                            );
                        }
                    } else if ("empresa".equals(tipoOrg)) {
                        if (usuario.getEmpresa() == null || !usuario.getEmpresa().getId().equals(orgId)) {
                            throw new org.springframework.security.access.AccessDeniedException(
                                    "Usuário representante não está vinculado a esta empresa."
                            );
                        }
                    }
                }
            }
        }
    }

    /**
     * Conta o número de vínculos de dependência ativos envolvendo uma organização.
     */
    private long contarVinculosAtivosPorOrganizacaoId(UUID organizacaoId) {
        Organizacao org = organizacaoRepository.findById(organizacaoId).orElse(null);
        if (org instanceof Empresa) {
            return vinculoDependenciaRepository.countActiveVinculosByEmpresaId(organizacaoId);
        } else if (org instanceof Clinica) {
            return vinculoDependenciaRepository.countActiveVinculosByClinicaId(organizacaoId);
        }
        return 0; 
    }

    /**
     * Conta o número de membros/funcionários vinculados a uma organização.
     */
    private int contarMembrosVinculados(UUID organizacaoId) {
        Organizacao org = organizacaoRepository.findById(organizacaoId).orElse(null);
        if (org instanceof Empresa) {
            return (int) usuarioRepository.countByEmpresaId(organizacaoId);
        } else if (org instanceof Clinica) {
            return (int) profissionalRepository.countByClinicaId(organizacaoId);
        }
        return 0;
    }

    /**
     * Inativa todos os vínculos de dependência associados a uma organização.
     */
    private void inativarVinculosDaOrganizacao(UUID organizacaoId) {
        Organizacao org = organizacaoRepository.findById(organizacaoId).orElse(null);
        List<VinculoDependencia> vinculos = List.of();
        if (org instanceof Empresa) {
            vinculos = vinculoDependenciaRepository.findActiveVinculosByEmpresaId(organizacaoId);
        } else if (org instanceof Clinica) {
            vinculos = vinculoDependenciaRepository.findActiveVinculosByClinicaId(organizacaoId);
        }
        for (VinculoDependencia v : vinculos) {
            v.setDataFim(java.time.LocalDate.now());
            vinculoDependenciaRepository.save(v);
        }
    }

    /**
     * Remove todos os membros vinculados a uma organização.
     */
    private void removerMembrosVinculados(UUID organizacaoId) {
        Organizacao org = organizacaoRepository.findById(organizacaoId).orElse(null);
        if (org instanceof Empresa) {
            List<Usuario> usuarios = usuarioRepository.findByEmpresaId(organizacaoId);
            for (Usuario u : usuarios) {
                u.setEmpresa(null);
                usuarioRepository.save(u);
            }
        } else if (org instanceof Clinica) {
            List<Profissional> profissionais = profissionalRepository.findByClinicaId(organizacaoId);
            for (Profissional p : profissionais) {
                profissionalRepository.delete(p);
            }
            List<Usuario> usuariosClinica = usuarioRepository.findByClinicaId(organizacaoId);
            for (Usuario u : usuariosClinica) {
                u.setClinica(null);
                usuarioRepository.save(u);
            }
        }
    }

    /**
     * Record auxiliar para resposta genérica de organizações.
     */
    private record OrganizacaoResponseDTO(
            UUID id,
            String nome,
            String cnpj,
            String tipo
    ) {
    }
}
