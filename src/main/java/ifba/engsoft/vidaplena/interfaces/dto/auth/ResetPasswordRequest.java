package ifba.engsoft.vidaplena.interfaces.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para redefinição de senha com token")
public record ResetPasswordRequest(
		@Schema(description = "Token criptográfico recebido por e-mail", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank String token,

		@Schema(description = "Nova senha segura do usuário (mínimo de 8 caracteres)", example = "NovaSenhaForte@2026", minLength = 8, maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Size(min = 8, max = 100) String novaSenha) {
}