package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de retorno de documento ou imagem anexada ao prontuário eletrônico")
public record DocumentoProntuarioResponseDTO(
    @Schema(description = "Identificador único do documento anexado", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "Identificador único do prontuário vinculado", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID prontuarioId,

    @Schema(description = "Identificador único do profissional de saúde responsável pelo anexo", example = "890e4567-e89b-12d3-a456-426614174000")
    UUID profissionalId,

    @Schema(description = "Nome do profissional de saúde", example = "Dr. Carlos Eduardo Menezes")
    String profissionalNome,

    @Schema(description = "Registro de conselho do profissional", example = "CRM/BA 12345")
    String profissionalRegistroConselho,

    @Schema(description = "Identificador único do atendimento clínico associado (opcional)", example = "111e4567-e89b-12d3-a456-426614174000")
    UUID registroAtendimentoId,

    @Schema(description = "Título descritivo do documento ou exame", example = "Hemograma Completo e Lipidograma")
    String titulo,

    @Schema(description = "Observações clínicas sobre o documento anexado", example = "Exames laboratoriais pré-operatórios de rotina.")
    String descricao,

    @Schema(description = "Tipo/categoria do documento anexado", example = "EXAME_LABORATORIAL")
    TipoDocumento tipoDocumento,

    @Schema(description = "Nome original do arquivo enviado", example = "laudo_laboratorio_2026.pdf")
    String nomeOriginal,

    @Schema(description = "Tipo de conteúdo MIME do arquivo", example = "application/pdf")
    String tipoConteudo,

    @Schema(description = "Tamanho do arquivo em bytes", example = "1048576")
    Long tamanhoBytes,

    @Schema(description = "Data de emissão ou realização do exame", example = "2026-08-20")
    LocalDate dataDocumento,

    @Schema(description = "Data e hora em que o documento foi anexado ao sistema", example = "2026-08-26T14:30:00")
    LocalDateTime dataCriacao,

    @Schema(description = "URL relativa para download ou visualização do arquivo", example = "/api/v1/prontuarios/documentos/3fa85f64-5717-4562-b3fc-2c963f66afa6/download")
    String downloadUrl
) {}
