package ifba.engsoft.vidaplena.interfaces.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import ifba.engsoft.vidaplena.domain.model.StatusUsuario;

@Schema(description = "Requisição para alteração do status da conta de um usuário")
public record AlterarStatusUsuarioRequest(
		@Schema(description = "Novo status da conta do usuário", example = "ATIVO", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull StatusUsuario status) {
}