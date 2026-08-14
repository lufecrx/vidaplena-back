package ifba.engsoft.vidaplena.domain.dto.familia;

import ifba.engsoft.vidaplena.domain.model.familia.TipoDependencia;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Dados de retorno do vínculo de dependência familiar")
public record VinculoDependenciaResponseDTO(
    @Schema(description = "Identificador único do vínculo", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
    UUID id,

    @Schema(description = "Nome completo do usuário responsável", example = "Maria da Silva")
    String responsavelNome,

    @Schema(description = "Nome completo do usuário dependente", example = "Lucas Silva Santos")
    String dependenteNome,

    @Schema(description = "Classificação do tipo de dependência", example = "CRIANCA")
    TipoDependencia tipo,

    @Schema(description = "Data de início do vínculo", example = "2026-01-01")
    LocalDate dataInicio,

    @Schema(description = "Data de término do vínculo", example = "2028-12-31")
    LocalDate dataFim
) {
}