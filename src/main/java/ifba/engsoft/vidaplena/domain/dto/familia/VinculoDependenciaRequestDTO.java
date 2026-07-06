package ifba.engsoft.vidaplena.domain.dto.familia;

import ifba.engsoft.vidaplena.domain.model.familia.TipoDependencia;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.UUID;

public record VinculoDependenciaRequestDTO(
    @NotNull(message = "O ID do responsável é obrigatório")
    UUID responsavelId,
    
    @NotNull(message = "O ID do dependente é obrigatório")
    UUID dependenteId,
    
    @NotNull(message = "O tipo de dependência é obrigatório")
    TipoDependencia tipo,
    
    @NotNull(message = "A data de início é obrigatória")
    @PastOrPresent(message = "A data de início não pode ser no futuro")
    LocalDate dataInicio,
    
    @FutureOrPresent(message = "A data de fim não pode ser no passado")
    LocalDate dataFim
) {
}