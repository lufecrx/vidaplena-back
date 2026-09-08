package ifba.engsoft.vidaplena.domain.dto.prontuario;

import ifba.engsoft.vidaplena.domain.model.prontuario.DocumentoVersao;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de retorno do histórico de versão de um documento do prontuário")
public record DocumentoVersaoResponse(
    @Schema(description = "Identificador único da versão", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Número da versão do documento", example = "2")
    Integer versao,

    @Schema(description = "Nome do arquivo nesta versão", example = "exame_sangue_v2.pdf")
    String nomeArquivo,

    @Schema(description = "Tamanho do arquivo em bytes", example = "1048576")
    Long tamanhoBytes,

    @Schema(description = "Tipo de conteúdo do arquivo", example = "application/pdf")
    String tipoConteudo,

    @Schema(description = "Data e hora em que esta versão foi criada", example = "2026-09-08T02:24:12")
    LocalDateTime dataCriacao
) {
    public static DocumentoVersaoResponse from(DocumentoVersao versaoEntity) {
        if (versaoEntity == null) {
            return null;
        }
        return new DocumentoVersaoResponse(
            versaoEntity.getId(),
            versaoEntity.getVersao(),
            versaoEntity.getNomeArquivo(),
            versaoEntity.getTamanhoBytes(),
            versaoEntity.getTipoConteudo(),
            versaoEntity.getDataCriacao()
        );
    }
}