package ifba.engsoft.vidaplena.interfaces.dto.usuario;

import jakarta.validation.constraints.NotNull;

import ifba.engsoft.vidaplena.domain.model.StatusUsuario;

public record AlterarStatusUsuarioRequest(@NotNull StatusUsuario status) {
}