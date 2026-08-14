package ifba.engsoft.vidaplena.domain.dto.organizacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Dados para cadastro de nova empresa conveniada")
public record EmpresaRequestDTO(
    @Schema(description = "Razão social ou nome fantasia da empresa", example = "Tech Solutions Ltda", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O nome da empresa é obrigatório")
    String nome,
    
    @Schema(description = "CNPJ com exatamente 14 dígitos numéricos (sem pontuação)", example = "98765432000188", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O CNPJ é obrigatório")
    @Pattern(regexp = "^\\d{14}$", message = "O CNPJ deve conter exatamente 14 dígitos numéricos")
    String cnpj,
    
    @Schema(description = "Setor ou segmento de atuação da empresa", example = "TECNOLOGIA_INFORMACAO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O setor da empresa é obrigatório")
    String setor
) {
}