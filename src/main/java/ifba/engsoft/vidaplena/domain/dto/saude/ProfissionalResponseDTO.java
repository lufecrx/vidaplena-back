package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Especialidade;
import java.util.UUID;

public record ProfissionalResponseDTO(
    UUID id,
    String usuarioId,
    String registroConselho,
    Especialidade especialidade,
    UUID clinicaId
) {
}