package ifba.engsoft.vidaplena.domain.dto.familia;

import ifba.engsoft.vidaplena.domain.model.familia.TipoDependencia;
import ifba.engsoft.vidaplena.domain.model.saude.TipoSanguineo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Dados consolidados do dependente cadastrado e vinculado ao responsável")
public record DependenteResponseDTO(
    @Schema(description = "Identificador único do vínculo de dependência", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
    UUID vinculoId,

    @Schema(description = "Identificador único da conta de usuário do dependente", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8")
    UUID dependenteUsuarioId,

    @Schema(description = "Identificador único do perfil clínico de paciente do dependente", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID pacienteId,

    @Schema(description = "Identificador único do usuário responsável", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID responsavelId,

    @Schema(description = "Nome completo do responsável", example = "Mariana Costa Silva")
    String responsavelNome,

    @Schema(description = "Nome completo do dependente", example = "Lucas Silva Santos")
    String nome,

    @Schema(description = "CPF do dependente", example = "12345678901")
    String cpf,

    @Schema(description = "E-mail de contato associado ao dependente", example = "dep.12345678901@dependente.vidaplena.local")
    String email,

    @Schema(description = "Telefone de contato", example = "(71) 98888-7777")
    String telefone,

    @Schema(description = "Data de nascimento do dependente", example = "2020-05-15")
    LocalDate dataNascimento,

    @Schema(description = "Idade do dependente em anos completos", example = "6")
    int idade,

    @Schema(description = "Classificação da dependência: CRIANCA, IDOSO ou NECESSIDADE_ESPECIAL", example = "CRIANCA")
    TipoDependencia tipo,

    @Schema(description = "Data de início da vigência do vínculo", example = "2026-01-01")
    LocalDate dataInicio,

    @Schema(description = "Data de término do vínculo (se houver)", example = "2030-12-31")
    LocalDate dataFim,

    @Schema(description = "Fator e tipo sanguíneo do dependente", example = "O_POSITIVO")
    TipoSanguineo tipoSanguineo,

    @Schema(description = "Lista de alergias registradas", example = "[\"Dipirona\", \"Amendoim\"]")
    List<String> alergias,

    @Schema(description = "Lista de medicamentos de uso contínuo", example = "[\"Insulina\"]")
    List<String> medicamentosContinuos,

    @Schema(description = "Histórico clínico familiar ou observações de saúde", example = "Histórico familiar de asma e rinite alérgica.")
    String historicoFamiliar
) {
}
