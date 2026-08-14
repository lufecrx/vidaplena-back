package ifba.engsoft.vidaplena.domain.dto.saude;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Dados de retorno do registro de atendimento clínico")
public record RegistroAtendimentoResponseDTO(
    @Schema(description = "Identificador único do registro de atendimento", example = "111e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Identificador único do prontuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID prontuarioId,

    @Schema(description = "Identificador único do profissional", example = "890e4567-e89b-12d3-a456-426614174000")
    UUID profissionalId,

    @Schema(description = "Nome do profissional de saúde", example = "Dr. Carlos Eduardo")
    String profissionalNome,

    @Schema(description = "Registro de conselho do profissional", example = "CRM/BA 123456")
    String profissionalRegistroConselho,

    @Schema(description = "Identificador único do agendamento vinculado", example = "999e4567-e89b-12d3-a456-426614174000")
    UUID agendamentoId,

    @Schema(description = "Data e hora do registro", example = "2026-09-01T15:00:00")
    LocalDateTime dataRegistro,

    @Schema(description = "Sintomas e queixas relatados", example = "Cefaleia constante e fadiga há 3 dias.")
    String sintomasRelatados,

    @Schema(description = "Diagnóstico registrado", example = "Cefaleia tensional (CID G44.2)")
    String diagnostico,

    @Schema(description = "Prescrição médica fornecida", example = "Paracetamol 750mg de 8 em 8 horas se dor.")
    String prescricaoMedica,

    @Schema(description = "Prescrição de enfermagem fornecida", example = "Aferir pressão arterial a cada 6 horas.")
    String prescricaoEnfermagem,

    @Schema(description = "Notas clínicas e evolução", example = "Exame físico neurológico sem alterações focais.")
    String notasClinicas,

    @Schema(description = "Indica se o atendimento está finalizado e imutável", example = "true")
    boolean finalizado,

    @Schema(description = "Lista de adendos ou notas de retificação vinculadas a este atendimento")
    List<NotaRetificacaoResponseDTO> notasRetificacao
) {}

