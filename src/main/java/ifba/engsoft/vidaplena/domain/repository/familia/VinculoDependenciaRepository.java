package ifba.engsoft.vidaplena.domain.repository.familia;

import ifba.engsoft.vidaplena.domain.model.familia.VinculoDependencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface VinculoDependenciaRepository extends JpaRepository<VinculoDependencia, UUID> {
}