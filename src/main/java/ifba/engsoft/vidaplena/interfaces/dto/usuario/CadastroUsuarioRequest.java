package ifba.engsoft.vidaplena.interfaces.dto.usuario;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroUsuarioRequest(
		@NotBlank String nome,
		@NotBlank String cpf,
		@NotBlank @Email String email,
		@NotBlank @Size(min = 8, max = 100) String senha,
		String telefone,
		LocalDate dataNascimento) {
}