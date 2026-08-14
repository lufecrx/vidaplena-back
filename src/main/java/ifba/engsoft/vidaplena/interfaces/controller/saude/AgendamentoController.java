package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.CriarAgendamentoRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.AgendamentoResponseDTO;
import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import ifba.engsoft.vidaplena.domain.service.saude.AgendamentoService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/agendamentos", "/api/agendamentos"})
@CrossOrigin(origins = "*")
@Tag(name = "Agendamentos", description = "Agendamento de consultas e atendimentos clínicos, consulta de agenda por profissional, confirmação e cancelamento")
@SecurityRequirement(name = "bearerAuth")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

    @Operation(
            summary = "Criar novo agendamento",
            description = "Realiza o agendamento de um atendimento entre paciente e profissional em uma clínica. Valida disponibilidade de horário e conflitos.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Agendamento criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AgendamentoResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos de agendamento (data no passado ou campos obrigatórios ausentes)",
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
                    description = "Regra de negócio violada (ex: conflito de horário do profissional ou paciente)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMINISTRADOR', 'RESPONSAVEL', 'FUNCIONARIO_ADMINISTRATIVO', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'CUIDADOR')")
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamento(
            @Valid @RequestBody CriarAgendamentoRequestDTO request,
            UriComponentsBuilder uriBuilder) {
        AgendamentoResponseDTO response = agendamentoService.criarAgendamento(request);
        URI uri = uriBuilder.path("/api/v1/agendamentos/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(
            summary = "Listar agendamentos por profissional de saúde",
            description = "Recupera a agenda de atendimentos associada a um profissional de saúde.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de agendamentos retornada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AgendamentoResponseDTO.class)))),
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
    @GetMapping("/profissional/{profissionalId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR', 'FUNCIONARIO_ADMINISTRATIVO', 'PACIENTE', 'RESPONSAVEL')")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarAgendamentosPorProfissional(
            @Parameter(description = "Identificador único (UUID) do profissional de saúde", example = "890e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID profissionalId) {
        List<AgendamentoResponseDTO> response = agendamentoService.listarPorProfissional(profissionalId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Confirmar agendamento",
            description = "Atualiza a situação do agendamento para CONFIRMADO.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Agendamento confirmado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AgendamentoResponseDTO.class))),
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
                    description = "Agendamento não encontrado para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Agendamento em estado que não permite confirmação",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMINISTRADOR', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'FUNCIONARIO_ADMINISTRATIVO')")
    public ResponseEntity<AgendamentoResponseDTO> confirmarAgendamento(
            @Parameter(description = "Identificador único (UUID) do agendamento", example = "999e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        Agendamento agendamento = agendamentoService.atualizarStatus(id, StatusAgendamento.CONFIRMADO);
        return ResponseEntity.ok(agendamentoService.mapearParaResponseDTO(agendamento));
    }

    @Operation(
            summary = "Cancelar agendamento",
            description = "Atualiza a situação do agendamento para CANCELADO.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Agendamento cancelado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AgendamentoResponseDTO.class))),
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
                    description = "Agendamento não encontrado para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Agendamento em estado que não permite cancelamento",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMINISTRADOR', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'FUNCIONARIO_ADMINISTRATIVO', 'RESPONSAVEL')")
    public ResponseEntity<AgendamentoResponseDTO> cancelarAgendamento(
            @Parameter(description = "Identificador único (UUID) do agendamento", example = "999e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        Agendamento agendamento = agendamentoService.atualizarStatus(id, StatusAgendamento.CANCELADO);
        return ResponseEntity.ok(agendamentoService.mapearParaResponseDTO(agendamento));
    }
}