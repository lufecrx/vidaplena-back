package ifba.engsoft.vidaplena.interfaces.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de novo usuário")
public record CadastroUsuarioRequest(
		@Schema(description = "Nome completo do usuário", example = "Maria da Silva", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank String nome,

		@Schema(description = "Número do CPF do usuário (apenas dígitos ou formatado)", example = "12345678901", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank String cpf,

		@Schema(description = "Endereço de e-mail válido para acesso e notificações", example = "maria.silva@vidaplena.com.br", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Email String email,

		@Schema(description = "Senha segura de acesso (mínimo de 8 caracteres)", example = "SenhaForte@2026", minLength = 8, maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Size(min = 8, max = 100) String senha,

		@Schema(description = "Telefone de contato com DDD", example = "(71) 98765-4321")
		String telefone,

		@Schema(description = "Data de nascimento do usuário no formato ISO (AAAA-MM-DD)", example = "1990-05-15")
		LocalDate dataNascimento) {
}