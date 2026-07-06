package ifba.engsoft.vidaplena.domain.dto.organizacao;

import java.util.UUID;

public record EmpresaResponseDTO(
    UUID id,
    String nome,
    String cnpj,
    String setor
) {
}