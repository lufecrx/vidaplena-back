package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Especialidade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Dados para cadastro ou atualização de profissional de saúde")
public record ProfissionalDTO(
    @Schema(description = "Identificador (UUID ou String) do usuário base", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID do usuário é obrigatório")
    String usuarioId,
    
    @Schema(description = "Número de inscrição no conselho de classe profissional", example = "CRM/BA 123456")
    String registroConselho,
    
    @Schema(description = "Especialidade médica ou área de atuação de saúde", example = "CARDIOLOGIA")
    Especialidade especialidade,
    
    @Schema(description = "Identificador único da clínica de vinculação principal", example = "456e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID da clínica é obrigatório")
    UUID clinicaId
) {
}