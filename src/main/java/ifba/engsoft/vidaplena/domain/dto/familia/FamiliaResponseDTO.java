package ifba.engsoft.vidaplena.domain.dto.familia;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Dados de retorno do núcleo familiar e seus integrantes")
public record FamiliaResponseDTO(
    @Schema(description = "Identificador único da família", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "Nome do núcleo familiar", example = "Família Silva Santos")
    String nome,

    @Schema(description = "Lista dos membros vinculados à família")
    List<MembroResumo> membros
) {
    @Schema(description = "Resumo dos dados de um membro da família")
    public record MembroResumo(
        @Schema(description = "Identificador único do usuário membro", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Nome completo do membro", example = "Maria da Silva")
        String nome
    ) {}
}