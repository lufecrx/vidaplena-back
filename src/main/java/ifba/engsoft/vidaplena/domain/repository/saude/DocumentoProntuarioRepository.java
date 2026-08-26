package ifba.engsoft.vidaplena.domain.repository.saude;

import ifba.engsoft.vidaplena.domain.model.saude.DocumentoProntuario;
import ifba.engsoft.vidaplena.domain.model.saude.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentoProntuarioRepository extends JpaRepository<DocumentoProntuario, UUID> {

    List<DocumentoProntuario> findAllByProntuarioIdOrderByDataCriacaoDesc(UUID prontuarioId);

    List<DocumentoProntuario> findAllByProntuarioIdAndTipoDocumentoOrderByDataCriacaoDesc(UUID prontuarioId, TipoDocumento tipoDocumento);

    List<DocumentoProntuario> findAllByRegistroAtendimentoIdOrderByDataCriacaoDesc(UUID registroAtendimentoId);

    List<DocumentoProntuario> findAllByProfissionalIdOrderByDataCriacaoDesc(UUID profissionalId);
}
