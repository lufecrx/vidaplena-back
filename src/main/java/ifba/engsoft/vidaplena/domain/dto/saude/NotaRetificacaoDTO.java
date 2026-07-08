package ifba.engsoft.vidaplena.domain.dto.saude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record NotaRetificacaoDTO(
    @NotNull(message = "O ID do registro de atendimento é obrigatório")
    UUID registroAtendimentoId,
    
    @NotNull(message = "O ID do profissional é obrigatório")
    UUID profissionalId,
    
    @NotBlank(message = "O texto de retificação não pode estar em branco")
    String texto
) {}
