package ifba.engsoft.vidaplena.interfaces.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.Usuario;

@Schema(description = "Dados de retorno do perfil de usuário")
public record UsuarioResponse(
		@Schema(description = "Identificador único do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
		UUID id,

		@Schema(description = "Nome completo do usuário", example = "Maria da Silva")
		String nome,

		@Schema(description = "CPF cadastrado do usuário", example = "12345678901")
		String cpf,

		@Schema(description = "E-mail de acesso e comunicação", example = "maria.silva@vidaplena.com.br")
		String email,

		@Schema(description = "Telefone de contato", example = "(71) 98765-4321")
		String telefone,

		@Schema(description = "Data de nascimento", example = "1990-05-15")
		LocalDate dataNascimento,

		@Schema(description = "Status atual da conta no sistema", example = "ATIVO")
		StatusUsuario status,

		@Schema(description = "Perfis/papéis de acesso vinculados", example = "[\"PACIENTE\"]")
		Set<TipoUsuario> tipos) {

	public static UsuarioResponse from(Usuario usuario) {
		if (usuario == null) {
			return null;
		}
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNome(),
				usuario.getCpf(),
				usuario.getEmail(),
				usuario.getTelefone(),
				usuario.getDataNascimento(),
				usuario.getStatus(),
				usuario.getTipos() != null ? Set.copyOf(usuario.getTipos()) : Set.of());
	}
}