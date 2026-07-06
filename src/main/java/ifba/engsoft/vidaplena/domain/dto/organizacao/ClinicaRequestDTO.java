package ifba.engsoft.vidaplena.domain.dto.organizacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClinicaRequestDTO(
    @NotBlank(message = "O nome da clínica é obrigatório")
    String nome,
    
    @NotBlank(message = "O CNPJ é obrigatório")
    @Pattern(regexp = "^\\d{14}$", message = "O CNPJ deve conter exatamente 14 dígitos numéricos")
    String cnpj,
    
    @NotBlank(message = "O tipo da clínica é obrigatório")
    String tipo
) {
}