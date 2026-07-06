package ifba.engsoft.vidaplena.domain.repository.organizacao;

import ifba.engsoft.vidaplena.domain.model.organizacao.Organizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface OrganizacaoRepository extends JpaRepository<Organizacao, UUID> {
}