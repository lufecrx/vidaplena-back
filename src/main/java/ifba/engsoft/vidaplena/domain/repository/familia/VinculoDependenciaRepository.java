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
     * Busca todos os vínculos de um usuário como responsável (incluindo inativos).
     */
    List<VinculoDependencia> findByResponsavelId(UUID responsavelId);

    /**
     * Busca todos os vínculos ativos (sem dataFim) de um usuário como dependente.
     */
    List<VinculoDependencia> findByDependenteIdAndDataFimIsNull(UUID dependenteId);

    /**
     * Busca todos os vínculos de um usuário como dependente (incluindo inativos).
     */
    List<VinculoDependencia> findByDependenteId(UUID dependenteId);

    /**
     * Busca vínculos ativos entre uma família e seus membros.
     */
    List<VinculoDependencia> findByResponsavelIdInAndDataFimIsNull(List<UUID> responsavelIds);

    /**
     * Verifica se existe pelo menos um vínculo ativo envolvendo um usuário (como responsável ou dependente).
     */
    boolean existsByResponsavelIdOrDependenteIdAndDataFimIsNull(UUID responsavelId, UUID dependenteId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(v) FROM VinculoDependencia v WHERE v.dataFim IS NULL AND (v.responsavel.empresa.id = :empresaId OR v.dependente.empresa.id = :empresaId)")
    long countActiveVinculosByEmpresaId(UUID empresaId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(v) FROM VinculoDependencia v WHERE v.dataFim IS NULL AND (v.responsavel.clinica.id = :clinicaId OR v.dependente.clinica.id = :clinicaId)")
    long countActiveVinculosByClinicaId(UUID clinicaId);

    @org.springframework.data.jpa.repository.Query("SELECT v FROM VinculoDependencia v WHERE v.dataFim IS NULL AND (v.responsavel.empresa.id = :empresaId OR v.dependente.empresa.id = :empresaId)")
    List<VinculoDependencia> findActiveVinculosByEmpresaId(UUID empresaId);

    @org.springframework.data.jpa.repository.Query("SELECT v FROM VinculoDependencia v WHERE v.dataFim IS NULL AND (v.responsavel.clinica.id = :clinicaId OR v.dependente.clinica.id = :clinicaId)")
    List<VinculoDependencia> findActiveVinculosByClinicaId(UUID clinicaId);
}