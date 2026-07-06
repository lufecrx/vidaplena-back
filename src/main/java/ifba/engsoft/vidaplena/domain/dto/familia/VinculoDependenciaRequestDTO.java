package ifba.engsoft.vidaplena.domain.dto.familia;

import ifba.engsoft.vidaplena.domain.model.familia.TipoDependencia;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record VinculoDependenciaRequestDTO(
    @NotNull(message = "O ID do responsável é obrigatório")
    UUID responsavelId,
    
    @NotNull(message = "O ID do dependente é obrigatório")
    UUID dependenteId,
    
    @NotNull(message = "O tipo de dependência é obrigatório")
    TipoDependencia tipo,
    
    LocalDate dataInicio
) {
}