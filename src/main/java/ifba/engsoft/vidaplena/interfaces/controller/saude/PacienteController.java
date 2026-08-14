package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.PacienteDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.PacienteResponseDTO;
import ifba.engsoft.vidaplena.domain.service.saude.PacienteService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "*")
@Tag(name = "Pacientes", description = "Cadastro e gerenciamento do perfil clínico, histórico familiar, alergias e medicações de pacientes")
@SecurityRequirement(name = "bearerAuth")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @Operation(
            summary = "Cadastrar perfil de paciente",
            description = "Cria o perfil clínico de saúde vinculado a uma conta de usuário existente.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Perfil de paciente criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PacienteResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos fornecidos ou campo usuarioId ausente",
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
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<PacienteResponseDTO> criarPaciente(@Valid @RequestBody PacienteDTO dto) {
        PacienteResponseDTO paciente = pacienteService.criarPaciente(dto);
        return new ResponseEntity<>(paciente, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Atualizar dados clínicos do paciente",
            description = "Atualiza fator sanguíneo, alergias, medicações contínuas e histórico familiar do paciente.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dados do paciente atualizados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PacienteResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos de atualização",
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
                    responseCode = "404",
                    description = "Paciente não encontrado para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> atualizarPaciente(
            @Parameter(description = "Identificador único (UUID) do paciente", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @RequestBody PacienteDTO dto) {
        PacienteResponseDTO paciente = pacienteService.atualizarPaciente(id, dto);
        return new ResponseEntity<>(paciente, HttpStatus.OK);
    }

    @Operation(
            summary = "Obter dados do paciente por ID",
            description = "Recupera as informações detalhadas e perfil clínico do paciente.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Paciente localizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PacienteResponseDTO.class))),
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
                    description = "Paciente não encontrado para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> obterPaciente(
            @Parameter(description = "Identificador único (UUID) do paciente", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        PacienteResponseDTO paciente = pacienteService.obterPaciente(id);
        return new ResponseEntity<>(paciente, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar todos os pacientes",
            description = "Retorna a listagem de todos os pacientes cadastrados na plataforma.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pacientes retornada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PacienteResponseDTO.class)))),
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
    public ResponseEntity<List<PacienteResponseDTO>> listarPacientes() {
        List<PacienteResponseDTO> pacientes = pacienteService.listarPacientes();
        return new ResponseEntity<>(pacientes, HttpStatus.OK);
    }

    @Operation(
            summary = "Remover paciente",
            description = "Exclui o registro de paciente pelo seu UUID.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Paciente removido com sucesso (sem conteúdo de retorno)"),
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
                    description = "Paciente não encontrado para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPaciente(
            @Parameter(description = "Identificador único (UUID) do paciente a remover", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        pacienteService.deletarPaciente(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}