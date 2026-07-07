package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.TipoSanguineo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PacienteDTO(
    @NotNull(message = "O ID do usuário é obrigatório")
    String usuarioId,
    
    TipoSanguineo tipoSanguineo,
    
    List<String> alergias,
    
    List<String> medicamentosContinuos,
    
    @Size(max = 10000, message = "O histórico familiar não pode exceder 10000 caracteres")
    String historicoFamiliar
) {
}