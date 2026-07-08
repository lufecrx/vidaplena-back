package ifba.engsoft.vidaplena.domain.service.familia;

import ifba.engsoft.vidaplena.domain.dto.familia.FamiliaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.FamiliaResponseDTO;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.familia.Familia;
import ifba.engsoft.vidaplena.domain.model.familia.VinculoDependencia;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.FamiliaRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.VinculoDependenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FamiliaService {

    @Autowired
    private FamiliaRepository familiaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VinculoDependenciaRepository vinculoDependenciaRepository;

    /**
     * Cria uma nova família com os membros informados.
     * Valida se todos os IDs de usuários existem no sistema.
     * 
     * @param dto DTO contendo os dados da família a ser criada
     * @return DTO de resposta com os dados da família criada
     * @throws RegraNegocioException se algum membro não existir no sistema
     */
    @Transactional
    public FamiliaResponseDTO criarFamilia(FamiliaRequestDTO dto) {
        // Validar se todos os IDs de membros existem
        List<UUID> membrosIds = dto.membros() != null ? dto.membros() : List.of();
        if (!membrosIds.isEmpty()) {
            for (UUID id : membrosIds) {
                if (!usuarioRepository.existsById(id)) {
                    throw new RegraNegocioException("Usuário com ID " + id + " não encontrado");
                }
            }
        }

        // Criar a família
        Familia familia = new Familia(dto.nome());
        familia.setMembros(usuarioRepository.findAllById(membrosIds));

        Familia familiaSalva = familiaRepository.save(familia);

        return mapearParaResponseDTO(familiaSalva);
    }

    /**
     * Exclusão segura de uma família. Antes de excluir, 
     * verifica se existem vínculos de dependência ativos envolvendo membros desta família.
     * 
     * @param familiaId ID da família a ser excluída
     * @throws RegraNegocioException se a família não existir ou se houver vínculos ativos
     */
    @Transactional
    public void excluirFamilia(UUID familiaId) {
        // Verificar se a família existe
        Familia familia = familiaRepository.findById(familiaId)
                .orElseThrow(() -> new RegraNegocioException(
                        "Família com ID " + familiaId + " não encontrada."
                ));

        // Verificar se existem vínculos de dependência ativos envolvendo membros desta família
        long vinculosAtivos = familiaRepository
                .countActiveVinculosByFamiliaId(familiaId);

        if (vinculosAtivos > 0) {
            throw new RegraNegocioException(
                    "Não é possível excluir a família '" + familia.getNome() + "' (ID: " + familiaId + 
                    "). Existem " + vinculosAtivos + " vínculo(s) de dependência ativo(s) envolvendo seus membros. " +
                    "Por segurança, famílias com vínculos ativos devem ser inativadas logicamente antes da exclusão. " +
                    "Utilize o endpoint de inativação de vínculos primeiro."
            );
        }

        // Verificar se a família possui membros (mesmo sem vínculos)
        List<Usuario> membros = familia.getMembros();
        if (membros != null && !membros.isEmpty()) {
            throw new RegraNegocioException(
                    "Não é possível excluir a família '" + familia.getNome() + "' (ID: " + familiaId + 
                    "). Ela possui " + membros.size() + " membro(s) vinculado(s). " +
                    "Remova todos os membros antes de excluir a família."
            );
        }

        // Todas as validações passaram, proceder com a exclusão física
        familiaRepository.deleteById(familiaId);
    }

    /**
     * Soft delete (inativação) de uma família. 
     * Marca a família como inativa e remove todos os vínculos ativos de seus membros.
     * 
     * @param familiaId ID da família a ser inativada
     * @return DTO de resposta com os dados da família inativada
     * @throws RegraNegocioException se a família não existir
     */
    @Transactional
    public FamiliaResponseDTO inativarFamilia(UUID familiaId) {
        Familia familia = familiaRepository.findById(familiaId)
                .orElseThrow(() -> new RegraNegocioException(
                        "Família com ID " + familiaId + " não encontrada."
                ));

        // Remover todos os vínculos ativos dos membros desta família
        List<UUID> membrosIds = familia.getMembros().stream()
                .map(Usuario::getId)
                .collect(Collectors.toList());

        if (!membrosIds.isEmpty()) {
            List<VinculoDependencia> vinculosAtivos = vinculoDependenciaRepository
                    .findByResponsavelIdInAndDataFimIsNull(membrosIds);

            for (VinculoDependencia vinculo : vinculosAtivos) {
                vinculo.setDataFim(java.time.LocalDate.now());
                vinculoDependenciaRepository.save(vinculo);
            }
        }

        // Limpar os membros da família
        familia.setMembros(List.of());

        // Renomear para indicar inativação
        familia.setNome("[INATIVO] " + familia.getNome());

        Familia familiaInativada = familiaRepository.save(familia);

        return mapearParaResponseDTO(familiaInativada);
    }

    /**
     * Busca uma família pelo ID.
     * 
     * @param familiaId ID da família a ser buscada
     * @return DTO de resposta com os dados da família encontrada
     * @throws RegraNegocioException se a família não existir
     */
    @Transactional(readOnly = true)
    public FamiliaResponseDTO buscarFamilia(UUID familiaId) {
        Familia familia = familiaRepository.findById(familiaId)
                .orElseThrow(() -> new RegraNegocioException(
                        "Família com ID " + familiaId + " não encontrada."
                ));

        // Validar se o usuário logado pertence à família (para roles PACIENTE/RESPONSAVEL)
        // ou possui autorização profissional/admin para acessar os dados.
        org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
            if (usuario != null) {
                boolean isAdm = usuario.possuiTipo(ifba.engsoft.vidaplena.domain.model.TipoUsuario.ADMINISTRADOR);
                boolean isProfissional = usuario.possuiTipo(ifba.engsoft.vidaplena.domain.model.TipoUsuario.MEDICO)
                        || usuario.possuiTipo(ifba.engsoft.vidaplena.domain.model.TipoUsuario.NUTRICIONISTA)
                        || usuario.possuiTipo(ifba.engsoft.vidaplena.domain.model.TipoUsuario.PERSONAL_TRAINER)
                        || usuario.possuiTipo(ifba.engsoft.vidaplena.domain.model.TipoUsuario.CUIDADOR)
                        || usuario.possuiTipo(ifba.engsoft.vidaplena.domain.model.TipoUsuario.FUNCIONARIO_ADMINISTRATIVO);
                
                boolean isPaciente = usuario.possuiTipo(ifba.engsoft.vidaplena.domain.model.TipoUsuario.PACIENTE);
                boolean isResponsavel = usuario.possuiTipo(ifba.engsoft.vidaplena.domain.model.TipoUsuario.RESPONSAVEL);

                if (!isAdm && !isProfissional && (isPaciente || isResponsavel)) {
                    boolean pertence = familia.getMembros().stream()
                            .anyMatch(membro -> membro.getId().equals(usuario.getId()));
                    if (!pertence) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                "Usuário não pertence à família consultada e não possui permissão de acesso."
                        );
                    }
                }
            }
        }

        return mapearParaResponseDTO(familia);
    }

    /**
     * Busca todas as famílias.
     * 
     * @return Lista de DTOs de resposta com os dados das famílias encontradas
     */
    @Transactional(readOnly = true)
    public List<FamiliaResponseDTO> buscarTodasFamilias() {
        return familiaRepository.findAll().stream()
                .map(this::mapearParaResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mapeia um objeto Familia para o DTO de resposta.
     * 
     * @param familia o objeto Familia a ser mapeado
     * @return DTO de resposta contendo os dados da família
     */
    private FamiliaResponseDTO mapearParaResponseDTO(Familia familia) {
        List<FamiliaResponseDTO.MembroResumo> membros = familia.getMembros() != null
                ? familia.getMembros().stream()
                    .map(usuario -> new FamiliaResponseDTO.MembroResumo(
                            usuario.getId(), 
                            usuario.getNome()))
                    .collect(Collectors.toList())
                : List.of();

        return new FamiliaResponseDTO(
                familia.getId(),
                familia.getNome(),
                membros
        );
    }
}
