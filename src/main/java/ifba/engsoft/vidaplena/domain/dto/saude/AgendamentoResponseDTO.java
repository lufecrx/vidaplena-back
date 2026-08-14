package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import ifba.engsoft.vidaplena.domain.model.saude.TipoAtendimento;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoResponseDTO(
    UUID id,
    UUID pacienteId,
    String pacienteNome,
    UUID profissionalId,
    String profissionalNome,
    UUID clinicaId,
    String clinicaNome,
    LocalDateTime dataHora,
    StatusAgendamento status,
    TipoAtendimento tipoAtendimento,
    String observacoes
) {
    // Construtor de conveniência para respostas simplificadas
    public AgendamentoResponseDTO(UUID id, UUID pacienteId, String pacienteNome, UUID profissionalId,
                                  String profissionalNome, LocalDateTime dataHora, StatusAgendamento status) {
        this(id, pacienteId, pacienteNome, profissionalId, profissionalNome, null, null, dataHora, status, null, null);
    }
}