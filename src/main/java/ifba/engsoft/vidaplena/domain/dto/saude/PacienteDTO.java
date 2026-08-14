package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.TipoSanguineo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Dados para cadastro ou atualização do perfil clínico do paciente")
public record PacienteDTO(
    @Schema(description = "Identificador (UUID ou String) do usuário base", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID do usuário é obrigatório")
    String usuarioId,
    
    @Schema(description = "Fator sanguíneo e Rh do paciente", example = "O_POSITIVO")
    TipoSanguineo tipoSanguineo,
    
    @Schema(description = "Lista de substâncias e medicamentos que causam reação alérgica", example = "[\"Penicilina\", \"Dipirona\", \"Amendoim\"]")
    List<String> alergias,
    
    @Schema(description = "Lista de medicamentos de uso contínuo", example = "[\"Losartana 50mg\", \"Metformina 850mg\"]")
    List<String> medicamentosContinuos,
    
    @Schema(description = "Histórico clínico familiar relevante", example = "Pai hipertenso, mãe com histórico de diabetes tipo 2.", maxLength = 10000)
    @Size(max = 10000, message = "O histórico familiar não pode exceder 10000 caracteres")
    String historicoFamiliar
) {
}