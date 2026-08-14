package ifba.engsoft.vidaplena.interfaces.controller;

import ifba.engsoft.vidaplena.domain.dto.organizacao.ClinicaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.ClinicaResponseDTO;
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
@RequestMapping("/api/clinicas")
@Tag(name = "Clínicas", description = "Gestão de clínicas, policlínicas e unidades de saúde credenciadas")
@SecurityRequirement(name = "bearerAuth")
public class ClinicaController {

    @Autowired
    private OrganizacaoService organizacaoService;

    @Operation(
            summary = "Cadastrar nova clínica",
            description = "Registra uma nova clínica de atendimento de saúde no sistema. Acesso restrito a ADMINISTRADOR ou REPRESENTANTE_EMPRESA.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Clínica cadastrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClinicaResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de clínica inválidos (CNPJ fora do formato de 14 dígitos ou campos ausentes)",
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
    public ResponseEntity<ClinicaResponseDTO> criarClinica(@Valid @RequestBody ClinicaRequestDTO dto) {
        ClinicaResponseDTO clinica = organizacaoService.criarClinica(dto);
        return new ResponseEntity<>(clinica, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Buscar clínica por ID",
            description = "Recupera os detalhes de cadastro de uma clínica específica. Acesso restrito a ADMINISTRADOR ou REPRESENTANTE_EMPRESA.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Clínica localizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClinicaResponseDTO.class))),
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
                    description = "Clínica não encontrada para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'REPRESENTANTE_EMPRESA')")
    public ResponseEntity<ClinicaResponseDTO> buscarClinicaPorId(
            @Parameter(description = "Identificador único (UUID) da clínica", example = "456e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        ClinicaResponseDTO clinica = organizacaoService.buscarClinicaPorId(id);
        return new ResponseEntity<>(clinica, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar todas as clínicas",
            description = "Retorna a listagem de todas as clínicas de saúde cadastradas. Acesso restrito a ADMINISTRADOR ou REPRESENTANTE_EMPRESA.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de clínicas retornada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ClinicaResponseDTO.class)))),
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
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'REPRESENTANTE_EMPRESA')")
    public ResponseEntity<List<ClinicaResponseDTO>> listarClinicas() {
        List<ClinicaResponseDTO> clinicas = organizacaoService.listarClinicas();
        return new ResponseEntity<>(clinicas, HttpStatus.OK);
    }
}