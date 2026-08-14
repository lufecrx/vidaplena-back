package ifba.engsoft.vidaplena.interfaces.controller;

import ifba.engsoft.vidaplena.domain.dto.familia.FamiliaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.FamiliaResponseDTO;
import ifba.engsoft.vidaplena.domain.service.familia.FamiliaService;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/familias")
@Tag(name = "Famílias", description = "Gestão de núcleos familiares para monitoramento integrado e compartilhado de saúde")
@SecurityRequirement(name = "bearerAuth")
public class FamiliaController {

    @Autowired
    private FamiliaService familiaService;

    @Operation(
            summary = "Criar novo núcleo familiar",
            description = "Cadastra uma nova unidade familiar associando os membros iniciais. Acesso restrito a ADMINISTRADOR ou RESPONSAVEL.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Família criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FamiliaResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos fornecidos ou lista de membros vazia",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - Requer perfil ADMINISTRADOR ou RESPONSAVEL",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada (ex: membro inexistente)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RESPONSAVEL')")
    public ResponseEntity<FamiliaResponseDTO> criarFamilia(@Valid @RequestBody FamiliaRequestDTO dto) {
        FamiliaResponseDTO familia = familiaService.criarFamilia(dto);
        return new ResponseEntity<>(familia, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Buscar família por ID",
            description = "Recupera os detalhes de um núcleo familiar e a lista de membros cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Família localizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FamiliaResponseDTO.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado para o perfil do usuário logado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Família não encontrada para o ID fornecido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'CUIDADOR', 'FUNCIONARIO_ADMINISTRATIVO', 'RESPONSAVEL', 'PACIENTE')")
    public ResponseEntity<FamiliaResponseDTO> buscarFamiliaPorId(
            @Parameter(description = "Identificador único (UUID) do núcleo familiar", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
        FamiliaResponseDTO familia = familiaService.buscarFamilia(id);
        return new ResponseEntity<>(familia, HttpStatus.OK);
    }
}