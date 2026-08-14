package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.TipoAtendimento;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record CriarAgendamentoRequestDTO(
    @NotNull(message = "O ID do paciente é obrigatório")
    UUID pacienteId,

    @NotNull(message = "O ID do profissional é obrigatório")
    UUID profissionalId,

    @NotNull(message = "O ID da clínica é obrigatório")
    UUID clinicaId,

    UUID planoCorporativoId,

    @NotNull(message = "A data e hora são obrigatórias")
    @Future(message = "A data e hora do agendamento devem ser futuras")
    LocalDateTime dataHora,

    @NotNull(message = "O tipo de atendimento é obrigatório")
    TipoAtendimento tipoAtendimento,

    String observacoes
) {
    public CriarAgendamentoRequestDTO(UUID pacienteId, UUID profissionalId, UUID clinicaId,
                                     LocalDateTime dataHora, TipoAtendimento tipoAtendimento, String observacoes) {
        this(pacienteId, profissionalId, clinicaId, null, dataHora, tipoAtendimento, observacoes);
    }
}
