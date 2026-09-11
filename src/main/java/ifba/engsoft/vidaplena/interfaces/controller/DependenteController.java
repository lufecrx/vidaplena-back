package ifba.engsoft.vidaplena.interfaces.controller;

import ifba.engsoft.vidaplena.domain.dto.familia.CadastroDependenteRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.DependenteResponseDTO;
import ifba.engsoft.vidaplena.domain.service.familia.VinculoDependenciaService;
import ifba.engsoft.vidaplena.infrastructure.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/dependentes", "/api/dependentes"})
@CrossOrigin(origins = "*")
@Tag(name = "Dependentes", description = "Cadastro unificado e gerenciamento de dependentes familiares (crianças, idosos, PcD) vinculados ao perfil do responsável")
@SecurityRequirement(name = "bearerAuth")
public class DependenteController {

    @Autowired
    private VinculoDependenciaService vinculoDependenciaService;

    @Operation(
            summary = "Cadastrar dependente vinculado ao meu perfil",
            description = "Endpoint atômico e unificado para cadastro de dependentes (crianças, idosos, PcD). Cria a conta de usuário, perfil clínico de paciente e o vínculo de dependência em uma única transação.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Dependente cadastrado e vinculado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DependenteResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos de cadastro ou campos obrigatórios ausentes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado para o perfil do usuário",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada (auto-dependência, faixa etária incompatível com classificação, duplicidade de vínculo)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR', 'PACIENTE')")
    public ResponseEntity<DependenteResponseDTO> cadastrarDependente(
            @Valid @RequestBody CadastroDependenteRequestDTO dto,
            Authentication authentication) {
        String emailAutenticado = authentication != null ? authentication.getName() : null;
        DependenteResponseDTO response = vinculoDependenciaService.cadastrarDependente(dto, emailAutenticado);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Listar meus dependentes",
            description = "Recupera todos os dependentes com histórico clínico básico vinculados ao usuário responsável autenticado.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de dependentes retornada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DependenteResponseDTO.class)))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR', 'PACIENTE')")
    public ResponseEntity<List<DependenteResponseDTO>> listarDependentes(
            @RequestParam(defaultValue = "true") boolean apenasAtivos,
            @RequestParam(required = false) UUID responsavelId,
            Authentication authentication) {
        String emailAutenticado = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(vinculoDependenciaService.listarDependentes(emailAutenticado, responsavelId, apenasAtivos));
    }

    @Operation(
            summary = "Obter detalhes de dependente",
            description = "Recupera dados consolidados do dependente (cadastro, perfil clínico e vínculo) a partir do ID do vínculo de dependência.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dependente localizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DependenteResponseDTO.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: vínculo não pertence ao usuário autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vínculo de dependência não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{vinculoId}")
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR', 'PACIENTE', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'FUNCIONARIO_ADMINISTRATIVO')")
    public ResponseEntity<DependenteResponseDTO> obterDependente(
            @Parameter(description = "Identificador único (UUID) do vínculo de dependência", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
            @PathVariable UUID vinculoId,
            Authentication authentication) {
        String emailAutenticado = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(vinculoDependenciaService.obterDependente(vinculoId, emailAutenticado));
    }

    @Operation(
            summary = "Inativar vínculo de dependência",
            description = "Encerra a vigência do vínculo com o dependente (soft delete atribuindo dataFim).")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Vínculo inativado com sucesso"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vínculo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{vinculoId}")
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR')")
    public ResponseEntity<Void> inativarDependente(
            @Parameter(description = "Identificador único (UUID) do vínculo a ser inativado", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
            @PathVariable UUID vinculoId) {
        vinculoDependenciaService.inativarVinculo(vinculoId);
        return ResponseEntity.noContent().build();
    }
}
