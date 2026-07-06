package ifba.engsoft.vidaplena.domain.dto.familia;

import java.util.List;
import java.util.UUID;

public record FamiliaResponseDTO(
    UUID id,
    String nome,
    List<MembroResumo> membros
) {
    public record MembroResumo(UUID id, String nome) {}
}