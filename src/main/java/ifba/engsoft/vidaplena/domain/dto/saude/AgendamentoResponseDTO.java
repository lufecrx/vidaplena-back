package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import ifba.engsoft.vidaplena.domain.model.saude.TipoAtendimento;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de retorno do agendamento de consulta ou atendimento")
public record AgendamentoResponseDTO(
    @Schema(description = "Identificador único do agendamento", example = "999e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Identificador único do paciente", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID pacienteId,

    @Schema(description = "Nome completo do paciente", example = "Maria da Silva")
    String pacienteNome,

    @Schema(description = "Identificador único do profissional", example = "890e4567-e89b-12d3-a456-426614174000")
    UUID profissionalId,

    @Schema(description = "Nome completo do profissional de saúde", example = "Dr. Carlos Eduardo")
    String profissionalNome,

    @Schema(description = "Identificador único da clínica", example = "456e4567-e89b-12d3-a456-426614174000")
    UUID clinicaId,

    @Schema(description = "Nome da clínica", example = "Clínica Vida Plena Salvador")
    String clinicaNome,

    @Schema(description = "Data e horário agendado", example = "2026-09-01T14:30:00")
    LocalDateTime dataHora,

    @Schema(description = "Status atual do agendamento", example = "AGENDADO")
    StatusAgendamento status,

    @Schema(description = "Modalidade do atendimento", example = "PRESENCIAL")
    TipoAtendimento tipoAtendimento,

    @Schema(description = "Observações registradas", example = "Paciente solicita consulta de retorno de rotina.")
    String observacoes
) {
    // Construtor de conveniência para respostas simplificadas
    public AgendamentoResponseDTO(UUID id, UUID pacienteId, String pacienteNome, UUID profissionalId,
                                  String profissionalNome, LocalDateTime dataHora, StatusAgendamento status) {
        this(id, pacienteId, pacienteNome, profissionalId, profissionalNome, null, null, dataHora, status, null, null);
    }
}