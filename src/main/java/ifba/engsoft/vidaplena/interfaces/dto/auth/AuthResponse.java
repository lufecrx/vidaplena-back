package ifba.engsoft.vidaplena.interfaces.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.UsuarioResponse;

@Schema(description = "Resposta de autenticação bem-sucedida contendo o token JWT")
public record AuthResponse(
		@Schema(description = "Token de acesso JWT (Bearer token)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtYXJpYUBleGFtcGxlLmNvbSIsImV4cCI6MTc1MDAwMDAwMH0.signature")
		String accessToken,

		@Schema(description = "Tipo do token emitido", example = "Bearer")
		String tokenType,

		@Schema(description = "Dados do usuário autenticado")
		UsuarioResponse usuario) {
}