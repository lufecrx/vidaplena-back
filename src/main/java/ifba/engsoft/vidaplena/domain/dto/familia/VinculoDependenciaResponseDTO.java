package ifba.engsoft.vidaplena.domain.dto.familia;

import ifba.engsoft.vidaplena.domain.model.familia.TipoDependencia;
import java.time.LocalDate;
import java.util.UUID;

public record VinculoDependenciaResponseDTO(
    UUID id,
    String responsavelNome,
    String dependenteNome,
    TipoDependencia tipo,
    LocalDate dataInicio
) {
}