package ifba.engsoft.vidaplena.domain.dto.familia;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record FamiliaRequestDTO(
    @NotBlank(message = "O nome da família é obrigatório")
    String nome,
    
    List<UUID> membros
) {
}