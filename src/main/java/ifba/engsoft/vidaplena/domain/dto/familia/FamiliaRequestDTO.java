package ifba.engsoft.vidaplena.domain.dto.familia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record FamiliaRequestDTO(
    @NotBlank(message = "O nome da família é obrigatório")
    String nome,
    
    @NotEmpty(message = "A família deve possuir pelo menos um membro")
    List<UUID> membros
) {
}