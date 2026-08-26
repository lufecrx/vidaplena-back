package ifba.engsoft.vidaplena.interfaces.controller;

import ifba.engsoft.vidaplena.domain.dto.organizacao.EmpresaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.EmpresaResponseDTO;
import ifba.engsoft.vidaplena.domain.service.organizacao.OrganizacaoService;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/empresas", "/api/empresas"})
@Tag(name = "Empresas", description = "Gestão de empresas parceiras e planos de saúde corporativos")
@SecurityRequirement(name = "bearerAuth")
public class EmpresaController {

    @Autowired
    private OrganizacaoService organizacaoService;

    @Operation(
            summary = "Cadastrar nova empresa parceira",
            description = "Registra uma nova empresa corporativa conveniada. Acesso restrito a ADMINISTRADOR ou REPRESENTANTE_EMPRESA.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Empresa cadastrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmpresaResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos de empresa (CNPJ fora de formato ou campos obrigatórios ausentes)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - Requer perfil ADMINISTRADOR ou REPRESENTANTE_EMPRESA",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'REPRESENTANTE_EMPRESA')")
    public ResponseEntity<EmpresaResponseDTO> criarEmpresa(@Valid @RequestBody EmpresaRequestDTO dto) {
        EmpresaResponseDTO empresa = organizacaoService.criarEmpresa(dto);
        return new ResponseEntity<>(empresa, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Buscar empresa por ID",
            description = "Recupera os detalhes de cadastro de uma empresa específica.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Empresa localizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmpresaResponseDTO.class))),
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
                    description = "Empresa não encontrada para o ID fornecido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'REPRESENTANTE_EMPRESA', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'FUNCIONARIO_ADMINISTRATIVO', 'PACIENTE', 'RESPONSAVEL', 'CUIDADOR')")
    public ResponseEntity<EmpresaResponseDTO> buscarEmpresaPorId(
            @Parameter(description = "Identificador único (UUID) da empresa", example = "789e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        EmpresaResponseDTO empresa = organizacaoService.buscarEmpresaPorId(id);
        return new ResponseEntity<>(empresa, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar todas as empresas parceiras",
            description = "Retorna a lista completa de empresas cadastradas no sistema.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de empresas retornada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EmpresaResponseDTO.class)))),
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
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'REPRESENTANTE_EMPRESA', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'FUNCIONARIO_ADMINISTRATIVO', 'PACIENTE', 'RESPONSAVEL', 'CUIDADOR')")
    public ResponseEntity<List<EmpresaResponseDTO>> listarEmpresas() {
        List<EmpresaResponseDTO> empresas = organizacaoService.listarEmpresas();
        return new ResponseEntity<>(empresas, HttpStatus.OK);
    }
}

