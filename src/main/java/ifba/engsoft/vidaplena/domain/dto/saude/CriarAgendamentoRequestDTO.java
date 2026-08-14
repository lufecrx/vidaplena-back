package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.TipoAtendimento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados para criação de um novo agendamento de consulta ou atendimento")
public record CriarAgendamentoRequestDTO(
    @Schema(description = "Identificador único do paciente", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID do paciente é obrigatório")
    UUID pacienteId,

    @Schema(description = "Identificador único do profissional de saúde", example = "890e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID do profissional é obrigatório")
    UUID profissionalId,

    @Schema(description = "Identificador único da clínica de atendimento", example = "456e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID da clínica é obrigatório")
    UUID clinicaId,

    @Schema(description = "Identificador único do plano corporativo (opcional)", example = "789e4567-e89b-12d3-a456-426614174000")
    UUID planoCorporativoId,

    @Schema(description = "Data e horário futuro para o atendimento (formato ISO-8601: AAAA-MM-DDTHH:mm:ss)", example = "2026-09-01T14:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "A data e hora são obrigatórias")
    @Future(message = "A data e hora do agendamento devem ser futuras")
    LocalDateTime dataHora,

    @Schema(description = "Modalidade do atendimento", example = "PRESENCIAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O tipo de atendimento é obrigatório")
    TipoAtendimento tipoAtendimento,

    @Schema(description = "Observações ou orientações preliminares", example = "Paciente solicita consulta de retorno de rotina.")
    String observacoes
) {
    public CriarAgendamentoRequestDTO(UUID pacienteId, UUID profissionalId, UUID clinicaId,
                                     LocalDateTime dataHora, TipoAtendimento tipoAtendimento, String observacoes) {
        this(pacienteId, profissionalId, clinicaId, null, dataHora, tipoAtendimento, observacoes);
    }
}

