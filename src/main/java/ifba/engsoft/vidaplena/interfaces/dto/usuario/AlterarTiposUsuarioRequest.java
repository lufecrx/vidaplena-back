package ifba.engsoft.vidaplena.interfaces.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

import ifba.engsoft.vidaplena.domain.model.TipoUsuario;

@Schema(description = "Requisição para alteração dos tipos/papéis de um usuário")
public record AlterarTiposUsuarioRequest(
		@Schema(description = "Conjunto de papéis/perfis associados ao usuário", example = "[\"PACIENTE\", \"RESPONSAVEL\"]", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotEmpty Set<TipoUsuario> tipos) {
}