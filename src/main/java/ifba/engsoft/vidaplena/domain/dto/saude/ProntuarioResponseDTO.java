package ifba.engsoft.vidaplena.domain.dto.saude;

import java.util.List;
import java.util.UUID;

public record ProntuarioResponseDTO(
    UUID id,
    UUID pacienteId,
    String pacienteNome,
    String observacoesGerais,
    List<RegistroAtendimentoResponseDTO> registros
) {
    public ProntuarioResponseDTO(UUID id, UUID pacienteId, String pacienteNome, String observacoesGerais) {
        this(id, pacienteId, pacienteNome, observacoesGerais, List.of());
    }
}