package ifba.engsoft.vidaplena.domain.repository.familia;

import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.familia.VinculoDependencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface VinculoDependenciaRepository extends JpaRepository<VinculoDependencia, UUID> {

    /**
     * Busca vínculo ativo (sem dataFim) pelo objeto responsável e dependente.
     */
    VinculoDependencia findByResponsavelAndDependenteAndDataFimIsNull(Usuario responsavel, Usuario dependente);

    /**
     * Busca vínculo ativo (sem dataFim) pelos IDs do responsável e dependente.
     * Usado para validação de unicidade antes de criar um novo vínculo.
     */
    VinculoDependencia findByResponsavelIdAndDependenteIdAndDataFimIsNull(UUID responsavelId, UUID dependenteId);

    /**
     * Busca todos os vínculos ativos (sem dataFim) de um usuário como responsável.
     */
    List<VinculoDependencia> findByResponsavelIdAndDataFimIsNull(UUID responsavelId);

    /**
     * Busca todos os vínculos ativos (sem dataFim) de um usuário como dependente.
     */
    List<VinculoDependencia> findByDependenteIdAndDataFimIsNull(UUID dependenteId);

    /**
     * Busca vínculos ativos entre uma família e seus membros.
     */
    List<VinculoDependencia> findByResponsavelIdInAndDataFimIsNull(List<UUID> responsavelIds);

    /**
     * Verifica se existe pelo menos um vínculo ativo envolvendo um usuário (como responsável ou dependente).
     */
    boolean existsByResponsavelIdOrDependenteIdAndDataFimIsNull(UUID responsavelId, UUID dependenteId);
}