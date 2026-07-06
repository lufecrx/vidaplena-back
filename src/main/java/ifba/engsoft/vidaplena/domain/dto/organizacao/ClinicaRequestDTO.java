package ifba.engsoft.vidaplena.domain.dto.organizacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClinicaRequestDTO(
    @NotBlank(message = "O nome da clínica é obrigatório")
    String nome,
    
    @Pattern(regexp = "^\\d{14}$", message = "O CNPJ deve conter 14 dígitos")
    String cnpj,
    
    @NotBlank(message = "O tipo da clínica é obrigatório")
    String tipo
) {
}