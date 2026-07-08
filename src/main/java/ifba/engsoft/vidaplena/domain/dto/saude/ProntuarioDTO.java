package ifba.engsoft.vidaplena.domain.dto.saude;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ProntuarioDTO(
    UUID id,
    
    @NotNull(message = "O ID do paciente é obrigatório")
    UUID pacienteId,
    
    String observacoesGerais
) {}