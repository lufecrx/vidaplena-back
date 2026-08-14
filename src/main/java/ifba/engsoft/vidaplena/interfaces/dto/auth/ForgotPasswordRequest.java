package ifba.engsoft.vidaplena.interfaces.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para solicitação de redefinição de senha")
public record ForgotPasswordRequest(
		@Schema(description = "E-mail cadastrado na conta do usuário", example = "maria.silva@vidaplena.com.br", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Email String email) {
}