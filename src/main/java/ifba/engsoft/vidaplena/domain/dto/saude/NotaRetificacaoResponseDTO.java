package ifba.engsoft.vidaplena.domain.dto.saude;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotaRetificacaoResponseDTO(
    UUID id,
    UUID registroAtendimentoId,
    UUID profissionalId,
    String profissionalNome,
    String profissionalRegistroConselho,
    LocalDateTime dataRegistro,
    String texto
) {}
