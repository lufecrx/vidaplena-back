package ifba.engsoft.vidaplena.interfaces.dto.usuario;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;

public record UsuarioResponse(
		UUID id,
		String nome,
		String cpf,
		String email,
		String telefone,
		LocalDate dataNascimento,
		StatusUsuario status,
		Set<TipoUsuario> tipos) {
}