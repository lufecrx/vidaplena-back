package ifba.engsoft.vidaplena.domain.dto.familia;

import ifba.engsoft.vidaplena.domain.model.familia.TipoDependencia;
import ifba.engsoft.vidaplena.domain.model.saude.TipoSanguineo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Dados para cadastro unificado e atômico de dependente vinculado ao perfil do responsável")
public record CadastroDependenteRequestDTO(
    @Schema(description = "Identificador único do responsável (opcional; administradores podem informar para vincular a outro usuário; para usuários comuns é assumido o usuário autenticado)", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID responsavelId,

    @Schema(description = "Nome completo do dependente", example = "Lucas Silva Santos", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O nome do dependente é obrigatório")
    String nome,

    @Schema(description = "CPF do dependente (apenas dígitos ou formatado)", example = "12345678901", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O CPF do dependente é obrigatório")
    String cpf,

    @Schema(description = "Data de nascimento do dependente (formato AAAA-MM-DD)", example = "2020-05-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "A data de nascimento do dependente é obrigatória")
    @PastOrPresent(message = "A data de nascimento do dependente não pode ser futura")
    LocalDate dataNascimento,

    @Schema(description = "Classificação da dependência: CRIANCA, IDOSO ou NECESSIDADE_ESPECIAL", example = "CRIANCA", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O tipo de dependência é obrigatório")
    TipoDependencia tipo,

    @Schema(description = "E-mail de contato do dependente (opcional; se não informado, um identificador seguro será gerado automaticamente)", example = "lucas.dependente@vidaplena.com")
    String email,

    @Schema(description = "Telefone de contato do dependente (opcional)", example = "(71) 98888-7777")
    String telefone,

    @Schema(description = "Fator e tipo sanguíneo do dependente (opcional)", example = "O_POSITIVO")
    TipoSanguineo tipoSanguineo,

    @Schema(description = "Lista de alergias clínicas conhecidas do dependente (opcional)", example = "[\"Dipirona\", \"Amendoim\"]")
    List<String> alergias,

    @Schema(description = "Lista de medicamentos de uso contínuo utilizados pelo dependente (opcional)", example = "[\"Insulina\"]")
    List<String> medicamentosContinuos,

    @Schema(description = "Histórico clínico familiar ou observações de saúde do dependente (opcional)", example = "Histórico familiar paterno de hipertensão e diabetes.")
    String historicoFamiliar,

    @Schema(description = "Data de início da vigência da dependência (opcional, formato AAAA-MM-DD, padrão: data atual)", example = "2026-01-01")
    @PastOrPresent(message = "A data de início do vínculo não pode ser futura")
    LocalDate dataInicio,

    @Schema(description = "Data prevista de término do vínculo de dependência (opcional, formato AAAA-MM-DD)", example = "2030-12-31")
    LocalDate dataFim
) {
}
