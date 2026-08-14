package ifba.engsoft.vidaplena.interfaces.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vinculos")
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
    public ResponseEntity<VinculoDependenciaResponseDTO> criarVinculo(@Valid @RequestBody VinculoDependenciaRequestDTO dto) {
        VinculoDependenciaResponseDTO vinculo = vinculoDependenciaService.criarVinculo(dto);
        return new ResponseEntity<>(vinculo, HttpStatus.CREATED);
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
    public ResponseEntity<Void> removerVinculo(
            @Parameter(description = "Identificador único (UUID) do vínculo de dependência", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
            @PathVariable UUID id) {
        vinculoDependenciaService.inativarVinculo(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}