package ifba.engsoft.vidaplena.domain.dto.organizacao;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Dados de retorno do cadastro da empresa conveniada")
public record EmpresaResponseDTO(
    @Schema(description = "Identificador único da empresa", example = "789e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Nome da empresa", example = "Tech Solutions Ltda")
    String nome,

    @Schema(description = "CNPJ cadastrado da empresa", example = "98765432000188")
    String cnpj,

    @Schema(description = "Setor ou ramo de atuação da empresa", example = "TECNOLOGIA_INFORMACAO")
    String setor
) {
}