package ifba.engsoft.vidaplena.domain.dto.organizacao;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Dados de retorno do cadastro de clínica")
public record ClinicaResponseDTO(
    @Schema(description = "Identificador único da clínica", example = "456e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Nome da clínica", example = "Clínica Vida Plena Salvador")
    String nome,

    @Schema(description = "CNPJ cadastrado da clínica", example = "12345678000195")
    String cnpj,

    @Schema(description = "Tipo ou classificação da unidade clínica", example = "POLICLINICA")
    String tipo
) {
}