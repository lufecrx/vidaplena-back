package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.TipoSanguineo;
import java.util.List;
import java.util.UUID;

public record PacienteResponseDTO(
    UUID id,
    String usuarioId,
    TipoSanguineo tipoSanguineo,
    List<String> alergias,
    List<String> medicamentosContinuos,
    String historicoFamiliar
) {
}