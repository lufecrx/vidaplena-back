package ifba.engsoft.vidaplena.domain.repository.saude;

import ifba.engsoft.vidaplena.domain.model.saude.NotaRetificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotaRetificacaoRepository extends JpaRepository<NotaRetificacao, UUID> {
    List<NotaRetificacao> findAllByRegistroAtendimentoIdOrderByDataRegistroAsc(UUID registroAtendimentoId);
}
