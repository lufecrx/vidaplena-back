package ifba.engsoft.vidaplena.domain.repository.familia;

import ifba.engsoft.vidaplena.domain.model.familia.Familia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FamiliaRepository extends JpaRepository<Familia, UUID> {

    /**
     * Verifica se uma família existe pelo ID.
     */
    boolean existsById(UUID familiaId);

    /**
     * Busca famílias ativas (sem inativação) que possuem membros com vínculos de dependência ativos.
     */
    @Query("SELECT f FROM Familia f JOIN f.membros m LEFT JOIN VinculoDependencia v ON (v.responsavel = m OR v.dependente = m) AND v.dataFim IS NULL WHERE f.id = :familiaId")
    Optional<Familia> findWithActiveVinculos(UUID familiaId);

    /**
     * Conta quantos vínculos de dependência ativos existem para membros de uma família.
     */
    @Query("SELECT COUNT(v) FROM VinculoDependencia v JOIN Familia f JOIN f.membros m WHERE f.id = :familiaId AND v.dataFim IS NULL AND (v.responsavel.id = m.id OR v.dependente.id = m.id)")
    long countActiveVinculosByFamiliaId(UUID familiaId);
}