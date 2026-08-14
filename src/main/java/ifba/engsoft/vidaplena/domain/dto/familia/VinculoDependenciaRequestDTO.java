package ifba.engsoft.vidaplena.domain.dto.familia;

import ifba.engsoft.vidaplena.domain.model.familia.TipoDependencia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Dados para criação de vínculo de dependência familiar")
public record VinculoDependenciaRequestDTO(
    @Schema(description = "Identificador único do usuário responsável", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID do responsável é obrigatório")
    UUID responsavelId,
    
    @Schema(description = "Identificador único do usuário dependente", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID do dependente é obrigatório")
    UUID dependenteId,
    
    @Schema(description = "Classificação do tipo de dependência", example = "CRIANCA", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O tipo de dependência é obrigatório")
    TipoDependencia tipo,
    
    @Schema(description = "Data de início da vigência do vínculo (formato AAAA-MM-DD)", example = "2026-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "A data de início é obrigatória")
    @PastOrPresent(message = "A data de início não pode ser no futuro")
    LocalDate dataInicio,
    
    @Schema(description = "Data prevista de término do vínculo de dependência (opcional, formato AAAA-MM-DD)", example = "2028-12-31")
    @FutureOrPresent(message = "A data de fim não pode ser no passado")
    LocalDate dataFim
) {
}