package ifba.engsoft.vidaplena.interfaces.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para autenticação do usuário")
public record LoginRequest(
		@Schema(description = "E-mail de acesso do usuário", example = "maria.silva@vidaplena.com.br", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Email String email,

		@Schema(description = "Senha secreta de acesso do usuário", example = "Senha@Segura123", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank String senha) {
}