package ifba.engsoft.vidaplena.infrastructure.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Estrutura padronizada de resposta para erros da API")
public record ApiErrorResponse(
		@Schema(description = "Instante UTC em que a exceção/erro ocorreu", example = "2026-08-14T19:30:00Z")
		Instant timestamp,

		@Schema(description = "Código de status HTTP numérico", example = "400")
		int status,

		@Schema(description = "Nome/descrição oficial da classe de status HTTP", example = "Bad Request")
		String error,

		@Schema(description = "Mensagem resumida sobre a causa do erro", example = "Dados inválidos fornecidos")
		String message,

		@Schema(description = "Caminho da URI onde ocorreu a falha", example = "/api/v1/auth/login")
		String path,

		@Schema(description = "Lista detalhada de mensagens de erro ou validações que falharam", example = "[\"O campo email é obrigatório\", \"A senha deve conter no mínimo 8 caracteres\"]")
		List<String> details) {
}