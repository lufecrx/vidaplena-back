package ifba.engsoft.vidaplena.domain.repository.familia;

import ifba.engsoft.vidaplena.domain.model.familia.Familia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface FamiliaRepository extends JpaRepository<Familia, UUID> {
}