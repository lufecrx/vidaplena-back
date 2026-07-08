package ifba.engsoft.vidaplena.domain.dto.saude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RegistroAtendimentoResponseDTO(
    UUID id,
    UUID prontuarioId,
    UUID profissionalId,
    String profissionalNome,
    String profissionalRegistroConselho,
    UUID agendamentoId,
    LocalDateTime dataRegistro,
    String sintomasRelatados,
    String diagnostico,
    String prescricaoMedica,
    String prescricaoEnfermagem,
    String notasClinicas,
    boolean finalizado,
    List<NotaRetificacaoResponseDTO> notasRetificacao
) {}
