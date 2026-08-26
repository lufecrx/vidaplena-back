package ifba.engsoft.vidaplena.domain.dto.saude;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Dados de retorno do prontuário eletrônico do paciente")
public record ProntuarioResponseDTO(
    @Schema(description = "Identificador único do prontuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "Identificador único do paciente titular", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID pacienteId,

    @Schema(description = "Nome completo do paciente titular", example = "Maria da Silva")
    String pacienteNome,

    @Schema(description = "Observações gerais e anotações permanentes", example = "Paciente hipertenso em acompanhamento multidisciplinar.")
    String observacoesGerais,

    @Schema(description = "Lista dos registros de atendimento clínico vinculados a este prontuário")
    List<RegistroAtendimentoResponseDTO> registros,

    @Schema(description = "Lista dos documentos, exames e laudos anexados a este prontuário")
    List<DocumentoProntuarioResponseDTO> documentos
) {
    public ProntuarioResponseDTO(UUID id, UUID pacienteId, String pacienteNome, String observacoesGerais) {
        this(id, pacienteId, pacienteNome, observacoesGerais, List.of(), List.of());
    }

    public ProntuarioResponseDTO(UUID id, UUID pacienteId, String pacienteNome, String observacoesGerais, List<RegistroAtendimentoResponseDTO> registros) {
        this(id, pacienteId, pacienteNome, observacoesGerais, registros, List.of());
    }
}