package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.ProfissionalDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.ProfissionalResponseDTO;
import ifba.engsoft.vidaplena.domain.service.saude.ProfissionalService;
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
@RequestMapping("/api/profissionais")
@CrossOrigin(origins = "*")
@Tag(name = "Profissionais de Saúde", description = "Gestão do corpo clínico (médicos, nutricionistas, educadores físicos, cuidadores), especialidades e registros em conselhos")
@SecurityRequirement(name = "bearerAuth")
public class ProfissionalController {

    @Autowired
    private ProfissionalService profissionalService;

    @Operation(
            summary = "Cadastrar profissional de saúde",
            description = "Registra um novo profissional de saúde vinculado a uma clínica de atendimento.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Profissional cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfissionalResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos fornecidos ou campos obrigatórios ausentes",
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
    public ResponseEntity<ProfissionalResponseDTO> criarProfissional(@Valid @RequestBody ProfissionalDTO dto) {
        ProfissionalResponseDTO profissional = profissionalService.criarProfissional(dto);
        return new ResponseEntity<>(profissional, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Atualizar profissional de saúde",
            description = "Atualiza especialidade, clínica de vínculo ou registro de conselho do profissional.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profissional atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfissionalResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos para atualização",
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
                    description = "Profissional não encontrado para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalResponseDTO> atualizarProfissional(
            @Parameter(description = "Identificador único (UUID) do profissional", example = "890e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @RequestBody ProfissionalDTO dto) {
        ProfissionalResponseDTO profissional = profissionalService.atualizarProfissional(id, dto);
        return new ResponseEntity<>(profissional, HttpStatus.OK);
    }

    @Operation(
            summary = "Obter dados do profissional por ID",
            description = "Recupera as informações detalhadas e vínculos do profissional de saúde.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profissional localizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfissionalResponseDTO.class))),
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
                    description = "Profissional não encontrado para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalResponseDTO> obterProfissional(
            @Parameter(description = "Identificador único (UUID) do profissional", example = "890e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        ProfissionalResponseDTO profissional = profissionalService.obterProfissional(id);
        return new ResponseEntity<>(profissional, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar todos os profissionais de saúde",
            description = "Retorna a listagem de todos os profissionais de saúde cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de profissionais retornada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProfissionalResponseDTO.class)))),
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
    public ResponseEntity<List<ProfissionalResponseDTO>> listarProfissionais() {
        List<ProfissionalResponseDTO> profissionais = profissionalService.listarProfissionais();
        return new ResponseEntity<>(profissionais, HttpStatus.OK);
    }

    @Operation(
            summary = "Remover profissional de saúde",
            description = "Exclui o cadastro do profissional de saúde pelo seu UUID.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Profissional removido com sucesso (sem conteúdo de retorno)"),
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
                    description = "Profissional não encontrado para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProfissional(
            @Parameter(description = "Identificador único (UUID) do profissional a remover", example = "890e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        profissionalService.deletarProfissional(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}