package ifba.engsoft.vidaplena.domain.dto.organizacao;

import java.util.UUID;

public record ClinicaResponseDTO(
    UUID id,
    String nome,
    String cnpj,
    String tipo
) {
}