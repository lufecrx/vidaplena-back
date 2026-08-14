package ifba.engsoft.vidaplena.interfaces.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de mensagem simples informativa")
public record MessageResponse(
		@Schema(description = "Mensagem de confirmação ou instrução", example = "Senha redefinida com sucesso.")
		String message) {
}