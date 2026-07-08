package ifba.engsoft.vidaplena.domain.dto.saude;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegistroAtendimentoDTO(
    UUID id,
    
    @NotNull(message = "O ID do prontuário é obrigatório")
    UUID prontuarioId,
    
    @NotNull(message = "O ID do profissional é obrigatório")
    UUID profissionalId,
    
    @NotNull(message = "O ID do agendamento é obrigatório")
    UUID agendamentoId,
    
    String sintomasRelatados,
    String diagnostico,
    String prescricaoMedica,
    String prescricaoEnfermagem,
    String notasClinicas,
    boolean finalizado
) {}