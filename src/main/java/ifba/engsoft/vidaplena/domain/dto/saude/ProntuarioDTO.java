package ifba.engsoft.vidaplena.domain.dto.saude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Dados para criação de prontuário eletrônico de paciente")
public record ProntuarioDTO(
    @Schema(description = "Identificador único do prontuário (gerado automaticamente se omitido)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,
    
    @Schema(description = "Identificador único do paciente titular", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID do paciente é obrigatório")
    UUID pacienteId,
    
    @Schema(description = "Observações gerais e anotações permanentes sobre o histórico do paciente", example = "Paciente hipertenso em acompanhamento multidisciplinar contínuo.")
    String observacoesGerais
) {}