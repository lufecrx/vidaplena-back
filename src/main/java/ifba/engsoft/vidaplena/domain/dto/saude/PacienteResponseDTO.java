package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.TipoSanguineo;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.UsuarioResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Dados de retorno do perfil clínico do paciente")
public record PacienteResponseDTO(
    @Schema(description = "Identificador único do paciente", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Identificador do usuário associado", example = "550e8400-e29b-41d4-a716-446655440000")
    String usuarioId,

    @Schema(description = "Fator sanguíneo e Rh", example = "O_POSITIVO")
    TipoSanguineo tipoSanguineo,

    @Schema(description = "Lista de alergias diagnosticadas", example = "[\"Penicilina\", \"Dipirona\"]")
    List<String> alergias,

    @Schema(description = "Lista de medicamentos em uso contínuo", example = "[\"Losartana 50mg\"]")
    List<String> medicamentosContinuos,

    @Schema(description = "Histórico clínico familiar", example = "Pai hipertenso, mãe diabética.")
    String historicoFamiliar,

    @Schema(description = "Dados cadastrais completos do usuário associado")
    UsuarioResponse usuario
) {
    public PacienteResponseDTO(
            UUID id,
            String usuarioId,
            TipoSanguineo tipoSanguineo,
            List<String> alergias,
            List<String> medicamentosContinuos,
            String historicoFamiliar
    ) {
        this(id, usuarioId, tipoSanguineo, alergias, medicamentosContinuos, historicoFamiliar, null);
    }
}