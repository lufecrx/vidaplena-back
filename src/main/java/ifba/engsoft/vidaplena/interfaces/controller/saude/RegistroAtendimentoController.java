package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.RegistroAtendimentoDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.RegistroAtendimentoResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.NotaRetificacaoDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.NotaRetificacaoResponseDTO;
import ifba.engsoft.vidaplena.domain.model.saude.*;
import ifba.engsoft.vidaplena.domain.service.saude.ProntuarioService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/registros-atendimento", "/api/registros-atendimento"})
@CrossOrigin(origins = "*")
@Tag(name = "Registros de Atendimento", description = "Registros de consultas, evoluções clínicas, diagnósticos, prescrições e adendos de retificação")
@SecurityRequirement(name = "bearerAuth")
public class RegistroAtendimentoController {

    @Autowired
    private ProntuarioService prontuarioService;

    @Operation(
            summary = "Criar registro de atendimento clínico",
            description = "Registra a evolução, queixas, hipótese diagnóstica e prescrições de um atendimento no prontuário do paciente.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Registro de atendimento criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistroAtendimentoResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Campos obrigatórios ausentes (prontuarioId, profissionalId, agendamentoId)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - Requer perfil clínico ou administrador",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Prontuário ou entidade associada inexistente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR')")
    public ResponseEntity<RegistroAtendimentoResponseDTO> criarRegistroAtendimento(@Valid @RequestBody RegistroAtendimentoDTO dto) {
        RegistroAtendimento registro = new RegistroAtendimento();
        
        Prontuario prontuario = new Prontuario();
        prontuario.setId(dto.prontuarioId());
        registro.setProntuario(prontuario);

        Profissional profissional = new Profissional();
        profissional.setId(dto.profissionalId());
        registro.setProfissional(profissional);

        Agendamento agendamento = new Agendamento();
        agendamento.setId(dto.agendamentoId());
        registro.setAgendamento(agendamento);

        registro.setSintomasRelatados(dto.sintomasRelatados());
        registro.setDiagnostico(dto.diagnostico());
        registro.setPrescricaoMedica(dto.prescricaoMedica());
        registro.setPrescricaoEnfermagem(dto.prescricaoEnfermagem());
        registro.setNotasClinicas(dto.notasClinicas());
        registro.setFinalizado(dto.finalizado());

        RegistroAtendimento novoRegistro = prontuarioService.criarRegistroAtendimento(registro);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponseDTO(novoRegistro));
    }

    @Operation(
            summary = "Atualizar registro de atendimento em aberto",
            description = "Permite atualizar os dados do atendimento enquanto ele não estiver finalizado. Registros com finalizado=true tornam-se imutáveis e retornam HTTP 422.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro de atendimento atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistroAtendimentoResponseDTO.class))),
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
                    description = "Registro de atendimento não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Registro clínico imutável: atendimento já foi finalizado e não aceita modificações diretas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR')")
    public ResponseEntity<RegistroAtendimentoResponseDTO> atualizarRegistroAtendimento(
            @Parameter(description = "Identificador único (UUID) do registro de atendimento", example = "111e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @RequestBody RegistroAtendimentoDTO dto) {
        RegistroAtendimento registro = new RegistroAtendimento();
        registro.setSintomasRelatados(dto.sintomasRelatados());
        registro.setDiagnostico(dto.diagnostico());
        registro.setPrescricaoMedica(dto.prescricaoMedica());
        registro.setPrescricaoEnfermagem(dto.prescricaoEnfermagem());
        registro.setNotasClinicas(dto.notasClinicas());
        registro.setFinalizado(dto.finalizado());

        RegistroAtendimento registroAtualizado = prontuarioService.atualizarRegistroAtendimento(id, registro);
        return ResponseEntity.ok(mapToResponseDTO(registroAtualizado));
    }

    @Operation(
            summary = "Excluir registro de atendimento em aberto",
            description = "Exclui um registro de atendimento não finalizado. Registros finalizados são imutáveis e não podem ser excluídos (retornam HTTP 422).")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Registro de atendimento excluído com sucesso (sem conteúdo)"),
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
                    description = "Registro de atendimento não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Registro clínico imutável: atendimento já foi finalizado e não pode ser excluído",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR')")
    public ResponseEntity<Void> deletarRegistroAtendimento(
            @Parameter(description = "Identificador único (UUID) do registro a ser excluído", example = "111e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        prontuarioService.deletarRegistroAtendimento(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Adicionar nota de retificação ao atendimento",
            description = "Adiciona um adendo, correção ou esclarecimento auditável a um registro de atendimento já existente.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Nota de retificação vinculada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotaRetificacaoResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Texto da retificação ou profissionalId inválidos",
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
                    description = "Registro de atendimento ou profissional não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{id}/retificar")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR')")
    public ResponseEntity<NotaRetificacaoResponseDTO> adicionarNotaRetificacao(
            @Parameter(description = "Identificador único (UUID) do registro de atendimento a retificar", example = "111e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @RequestBody NotaRetificacaoDTO dto) {
        NotaRetificacao nota = prontuarioService.adicionarNotaRetificacao(id, dto.profissionalId(), dto.texto());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToNotaResponseDTO(nota));
    }

    @Operation(
            summary = "Obter registro de atendimento por ID",
            description = "Recupera os dados completos da evolução clínica, diagnóstico, prescrições e lista de notas de retificação associadas.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro de atendimento localizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistroAtendimentoResponseDTO.class))),
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
                    description = "Registro de atendimento não encontrado para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR', 'PACIENTE', 'RESPONSAVEL')")
    public ResponseEntity<RegistroAtendimentoResponseDTO> obterRegistroAtendimento(
            @Parameter(description = "Identificador único (UUID) do registro de atendimento", example = "111e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        RegistroAtendimento registro = prontuarioService.obterRegistroAtendimentoPorId(id);
        return ResponseEntity.ok(mapToResponseDTO(registro));
    }

    @Operation(
            summary = "Listar registros de atendimento por prontuário",
            description = "Retorna todos os atendimentos clínicos vinculados a um prontuário específico.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de atendimentos clínicos retornada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RegistroAtendimentoResponseDTO.class)))),
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
                    description = "Prontuário não encontrado para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/prontuario/{prontuarioId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR', 'PACIENTE', 'RESPONSAVEL')")
    public ResponseEntity<List<RegistroAtendimentoResponseDTO>> obterRegistrosPorProntuario(
            @Parameter(description = "Identificador único (UUID) do prontuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID prontuarioId) {
        List<RegistroAtendimento> registros = prontuarioService.obterRegistrosAtendimentoPorProntuario(prontuarioId);
        List<RegistroAtendimentoResponseDTO> response = registros.stream()
                .map(this::mapToResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    // Helper mappings
    private RegistroAtendimentoResponseDTO mapToResponseDTO(RegistroAtendimento registro) {
        List<NotaRetificacaoResponseDTO> notas = registro.getNotasRetificacao().stream()
                .map(this::mapToNotaResponseDTO)
                .toList();

        return new RegistroAtendimentoResponseDTO(
                registro.getId(),
                registro.getProntuario().getId(),
                registro.getProfissional().getId(),
                registro.getProfissional().getUsuario() != null ? registro.getProfissional().getUsuario().getNome() : "N/A",
                registro.getProfissional().getRegistroConselho(),
                registro.getAgendamento().getId(),
                registro.getDataRegistro(),
                registro.getSintomasRelatados(),
                registro.getDiagnostico(),
                registro.getPrescricaoMedica(),
                registro.getPrescricaoEnfermagem(),
                registro.getNotasClinicas(),
                registro.isFinalizado(),
                notas
        );
    }

    private NotaRetificacaoResponseDTO mapToNotaResponseDTO(NotaRetificacao nota) {
        return new NotaRetificacaoResponseDTO(
                nota.getId(),
                nota.getRegistroAtendimento().getId(),
                nota.getProfissional().getId(),
                nota.getProfissional().getUsuario() != null ? nota.getProfissional().getUsuario().getNome() : "N/A",
                nota.getProfissional().getRegistroConselho(),
                nota.getDataRegistro(),
                nota.getTexto()
        );
    }
}