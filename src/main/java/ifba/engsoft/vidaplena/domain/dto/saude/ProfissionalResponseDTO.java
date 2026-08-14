package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Especialidade;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Dados de retorno do cadastro de profissional de saúde")
public record ProfissionalResponseDTO(
    @Schema(description = "Identificador único do profissional", example = "890e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Identificador do usuário associado", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8")
    String usuarioId,

    @Schema(description = "Número do conselho profissional", example = "CRM/BA 123456")
    String registroConselho,

    @Schema(description = "Especialidade médica/área", example = "CARDIOLOGIA")
    Especialidade especialidade,

    @Schema(description = "Identificador único da clínica vinculada", example = "456e4567-e89b-12d3-a456-426614174000")
    UUID clinicaId
) {
}