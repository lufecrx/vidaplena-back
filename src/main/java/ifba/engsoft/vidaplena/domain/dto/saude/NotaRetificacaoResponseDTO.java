package ifba.engsoft.vidaplena.domain.dto.saude;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de retorno de uma nota de retificação")
public record NotaRetificacaoResponseDTO(
    @Schema(description = "Identificador único da nota de retificação", example = "222e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Identificador único do registro retificado", example = "111e4567-e89b-12d3-a456-426614174000")
    UUID registroAtendimentoId,

    @Schema(description = "Identificador único do profissional autor", example = "890e4567-e89b-12d3-a456-426614174000")
    UUID profissionalId,

    @Schema(description = "Nome do profissional autor", example = "Dr. Carlos Eduardo")
    String profissionalNome,

    @Schema(description = "Registro de conselho do profissional", example = "CRM/BA 123456")
    String profissionalRegistroConselho,

    @Schema(description = "Data e hora de emissão da retificação", example = "2026-09-01T16:15:00")
    LocalDateTime dataRegistro,

    @Schema(description = "Texto da nota de retificação", example = "Nota de retificação: A dosagem correta prescrita do medicamento é de 500mg.")
    String texto
) {}

