package ifba.engsoft.vidaplena.domain.dto.familia;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Schema(description = "Dados para criação de um novo núcleo familiar")
public record FamiliaRequestDTO(
    @Schema(description = "Nome identificador do núcleo familiar", example = "Família Silva Santos", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O nome da família é obrigatório")
    String nome,
    
    @Schema(description = "Lista de UUIDs dos usuários que farão parte do núcleo familiar", example = "[\"550e8400-e29b-41d4-a716-446655440000\", \"6ba7b810-9dad-11d1-80b4-00c04fd430c8\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "A família deve possuir pelo menos um membro")
    List<UUID> membros
) {
}