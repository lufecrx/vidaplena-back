package ifba.engsoft.vidaplena.domain.model.saude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo/categoria de documento ou imagem anexada ao prontuário")
public enum TipoDocumento {
    @Schema(description = "Laudo médico ou parecer clínico emitido por especialista")
    LAUDO,

    @Schema(description = "Exame laboratorial (sangue, urina, fezes, etc.)")
    EXAME_LABORATORIAL,

    @Schema(description = "Exame de imagem (Raio-X, Tomografia, Ressonância, Ultrassom, ECG, etc.)")
    EXAME_IMAGEM,

    @Schema(description = "Receita ou prescrição externa")
    RECEITA,

    @Schema(description = "Atestado ou declaração médica")
    ATESTADO,

    @Schema(description = "Relatório ou parecer multiprofissional (nutrição, fisioterapia, psicologia)")
    RELATORIO_CLINICO,

    @Schema(description = "Outros documentos e anexos clínicos")
    OUTROS
}
