package ifba.engsoft.vidaplena.interfaces.controller;

import ifba.engsoft.vidaplena.domain.dto.familia.CadastroDependenteRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.DependenteResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaResponseDTO;
import ifba.engsoft.vidaplena.domain.service.familia.VinculoDependenciaService;
import ifba.engsoft.vidaplena.infrastructure.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/vinculos", "/api/vinculos"})
@Tag(name = "Vínculos de Dependência", description = "Gestão de laços de dependência e responsabilidade entre usuários no núcleo familiar")
@SecurityRequirement(name = "bearerAuth")
public class VinculoDependenciaController {

    @Autowired
    private VinculoDependenciaService vinculoDependenciaService;

    @Operation(
            summary = "Criar vínculo de dependência",
            description = "Estabelece formalmente um vínculo de cuidado/dependência entre um responsável e um dependente (ex: idoso, criança, necessidade especial).")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Vínculo criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VinculoDependenciaResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de requisição inválidos ou campos obrigatórios não informados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada (ex: responsável ou dependente inexistente)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR')")
    public ResponseEntity<VinculoDependenciaResponseDTO> criarVinculo(@Valid @RequestBody VinculoDependenciaRequestDTO dto) {
        VinculoDependenciaResponseDTO vinculo = vinculoDependenciaService.criarVinculo(dto);
        return new ResponseEntity<>(vinculo, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Cadastrar dependente de forma unificada e atômica",
            description = "Cria a conta do dependente, o perfil clínico de paciente e estabelece o vínculo de cuidado/dependência diretamente ao perfil do responsável em uma única transação atômica.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Dependente cadastrado e vinculado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DependenteResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de requisição inválidos ou campos obrigatórios ausentes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada (idade incompatível, conflito de CPF, auto-dependência)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/dependentes")
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR', 'PACIENTE')")
    public ResponseEntity<DependenteResponseDTO> cadastrarDependente(
            @Valid @RequestBody CadastroDependenteRequestDTO dto,
            Authentication authentication) {
        String emailAutenticado = authentication != null ? authentication.getName() : null;
        DependenteResponseDTO response = vinculoDependenciaService.cadastrarDependente(dto, emailAutenticado);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Listar dependentes com dados clínicos consolidados",
            description = "Recupera todos os dependentes vinculados ao perfil do responsável autenticado.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de dependentes retornada com sucesso"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/dependentes")
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR', 'PACIENTE')")
    public ResponseEntity<java.util.List<DependenteResponseDTO>> listarMeusDependentes(
            @RequestParam(defaultValue = "true") boolean apenasAtivos,
            @RequestParam(required = false) UUID responsavelId,
            Authentication authentication) {
        String emailAutenticado = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(vinculoDependenciaService.listarDependentes(emailAutenticado, responsavelId, apenasAtivos));
    }

    @Operation(
            summary = "Obter detalhes de dependente por ID do vínculo",
            description = "Recupera as informações consolidadas do dependente pelo ID do vínculo.")
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
    @GetMapping("/dependentes/{vinculoId}")
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR', 'PACIENTE', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'FUNCIONARIO_ADMINISTRATIVO')")
    public ResponseEntity<DependenteResponseDTO> obterDependente(
            @Parameter(description = "Identificador único (UUID) do vínculo", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
            @PathVariable UUID vinculoId,
            Authentication authentication) {
        String emailAutenticado = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(vinculoDependenciaService.obterDependente(vinculoId, emailAutenticado));
    }

    @Operation(
            summary = "Obter vínculo de dependência por ID",
            description = "Recupera os detalhes de um vínculo de dependência específico.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Vínculo localizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VinculoDependenciaResponseDTO.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
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
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'FUNCIONARIO_ADMINISTRATIVO')")
    public ResponseEntity<VinculoDependenciaResponseDTO> obterVinculo(
            @Parameter(description = "Identificador único (UUID) do vínculo", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
            @PathVariable UUID id) {
        return ResponseEntity.ok(vinculoDependenciaService.obterPorId(id));
    }

    @Operation(
            summary = "Listar vínculos por responsável",
            description = "Recupera todos os vínculos de dependência de um responsável familiar.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de vínculos retornada com sucesso"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/responsavel/{responsavelId}")
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'FUNCIONARIO_ADMINISTRATIVO')")
    public ResponseEntity<java.util.List<VinculoDependenciaResponseDTO>> listarPorResponsavel(
            @Parameter(description = "Identificador único (UUID) do responsável", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
            @PathVariable UUID responsavelId,
            @RequestParam(defaultValue = "false") boolean apenasAtivos) {
        return ResponseEntity.ok(vinculoDependenciaService.listarPorResponsavel(responsavelId, apenasAtivos));
    }

    @Operation(
            summary = "Listar vínculos por dependente",
            description = "Recupera todos os vínculos onde o usuário é dependente.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de vínculos retornada com sucesso"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/dependente/{dependenteId}")
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR', 'PACIENTE', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'FUNCIONARIO_ADMINISTRATIVO')")
    public ResponseEntity<java.util.List<VinculoDependenciaResponseDTO>> listarPorDependente(
            @Parameter(description = "Identificador único (UUID) do dependente", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
            @PathVariable UUID dependenteId,
            @RequestParam(defaultValue = "false") boolean apenasAtivos) {
        return ResponseEntity.ok(vinculoDependenciaService.listarPorDependente(dependenteId, apenasAtivos));
    }

    @Operation(
            summary = "Inativar vínculo de dependência",
            description = "Encerra e inativa o vínculo de dependência previamente cadastrado.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Vínculo inativado com sucesso (sem conteúdo de retorno)"),
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
                    description = "Vínculo não encontrado para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR')")
    public ResponseEntity<Void> removerVinculo(
            @Parameter(description = "Identificador único (UUID) do vínculo de dependência", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
            @PathVariable UUID id) {
        vinculoDependenciaService.inativarVinculo(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}