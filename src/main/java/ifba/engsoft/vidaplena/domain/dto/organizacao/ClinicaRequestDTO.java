package ifba.engsoft.vidaplena.domain.dto.organizacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Dados para cadastro de nova clínica de saúde")
public record ClinicaRequestDTO(
    @Schema(description = "Nome fantasia ou razão social da clínica", example = "Clínica Vida Plena Salvador", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O nome da clínica é obrigatório")
    String nome,
    
    @Schema(description = "CNPJ com exatamente 14 dígitos numéricos (sem pontuação)", example = "12345678000195", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O CNPJ é obrigatório")
    @Pattern(regexp = "^\\d{14}$", message = "O CNPJ deve conter exatamente 14 dígitos numéricos")
    String cnpj,
    
    @Schema(description = "Tipo ou classificação da unidade clínica", example = "POLICLINICA", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O tipo da clínica é obrigatório")
    String tipo
) {
}