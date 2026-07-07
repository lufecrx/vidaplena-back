package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Especialidade;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProfissionalDTO(
    @NotNull(message = "O ID do usuário é obrigatório")
    String usuarioId,
    
    String registroConselho,
    
    Especialidade especialidade,
    
    @NotNull(message = "O ID da clínica é obrigatório")
    UUID clinicaId
) {
}