package ifba.engsoft.vidaplena.domain.dto.saude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Dados para inclusão de uma nota de retificação a um registro de atendimento")
public record NotaRetificacaoDTO(
    @Schema(description = "Identificador único do registro de atendimento a ser retificado", example = "111e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID do registro de atendimento é obrigatório")
    UUID registroAtendimentoId,
    
    @Schema(description = "Identificador único do profissional de saúde autor da retificação", example = "890e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID do profissional é obrigatório")
    UUID profissionalId,
    
    @Schema(description = "Texto explicativo detalhando a correção, adendo ou esclarecimento sobre o atendimento", example = "Nota de retificação: A dosagem correta prescrita do medicamento é de 500mg ao invés de 750mg.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O texto de retificação não pode estar em branco")
    String texto
) {}

