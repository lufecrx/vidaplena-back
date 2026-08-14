package ifba.engsoft.vidaplena.domain.dto.saude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Dados para criação ou atualização de um registro de atendimento clínico")
public record RegistroAtendimentoDTO(
    @Schema(description = "Identificador único do registro (opcional na criação)", example = "111e4567-e89b-12d3-a456-426614174000")
    UUID id,
    
    @Schema(description = "Identificador único do prontuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID do prontuário é obrigatório")
    UUID prontuarioId,
    
    @Schema(description = "Identificador único do profissional que realizou o atendimento", example = "890e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID do profissional é obrigatório")
    UUID profissionalId,
    
    @Schema(description = "Identificador único do agendamento correspondente", example = "999e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O ID do agendamento é obrigatório")
    UUID agendamentoId,
    
    @Schema(description = "Sintomas e queixas relatadas pelo paciente na anamnese", example = "Cefaleia constante e fadiga há 3 dias.")
    String sintomasRelatados,

    @Schema(description = "Hipótese diagnóstica ou CID registrado pelo profissional", example = "Cefaleia tensional (CID G44.2)")
    String diagnostico,

    @Schema(description = "Prescrição médica, medicamentos recomendados e posologia", example = "Paracetamol 750mg de 8 em 8 horas se dor.")
    String prescricaoMedica,

    @Schema(description = "Prescrições, condutas e orientações de enfermagem", example = "Aferir pressão arterial a cada 6 horas.")
    String prescricaoEnfermagem,

    @Schema(description = "Evolução clínica, observações do exame físico e notas complementares", example = "Exame físico neurológico sem alterações focais.")
    String notasClinicas,

    @Schema(description = "Flag indicando se o atendimento foi finalizado (registros finalizados tornam-se imutáveis)", example = "false")
    boolean finalizado
) {}