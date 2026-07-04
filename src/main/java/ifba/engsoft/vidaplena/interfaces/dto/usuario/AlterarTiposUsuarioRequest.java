package ifba.engsoft.vidaplena.interfaces.dto.usuario;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

import ifba.engsoft.vidaplena.domain.model.TipoUsuario;

public record AlterarTiposUsuarioRequest(@NotEmpty Set<TipoUsuario> tipos) {
}