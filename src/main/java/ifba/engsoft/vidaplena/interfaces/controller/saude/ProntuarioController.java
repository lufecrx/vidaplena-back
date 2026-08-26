package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.ProntuarioDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.ProntuarioResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.RegistroAtendimentoResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.NotaRetificacaoResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.DocumentoProntuarioResponseDTO;
import ifba.engsoft.vidaplena.domain.model.saude.Prontuario;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.model.saude.RegistroAtendimento;
import ifba.engsoft.vidaplena.domain.model.saude.NotaRetificacao;
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
@RequestMapping({"/api/v1/prontuarios", "/api/prontuarios"})
@CrossOrigin(origins = "*")
@Tag(name = "Prontuários", description = "Acesso a prontuários eletrônicos de pacientes e histórico clínico integrado")
@SecurityRequirement(name = "bearerAuth")
public class ProntuarioController {

    @Autowired
    private ProntuarioService prontuarioService;

    @Operation(
            summary = "Criar prontuário eletrônico",
            description = "Cria um novo prontuário clínico para o paciente informado. Acesso restrito a profissionais de saúde e administradores.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Prontuário criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProntuarioResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou pacienteId ausente",
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
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR')")
    public ResponseEntity<ProntuarioResponseDTO> criarProntuario(@Valid @RequestBody ProntuarioDTO prontuarioDTO) {
        Prontuario prontuario = new Prontuario();
        
        Paciente paciente = new Paciente();
        paciente.setId(prontuarioDTO.pacienteId());
        prontuario.setPaciente(paciente);
        prontuario.setObservacoesGerais(prontuarioDTO.observacoesGerais());
        
        Prontuario novoProntuario = prontuarioService.criarProntuario(prontuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponseDTO(novoProntuario));
    }

    @Operation(
            summary = "Obter prontuário por ID",
            description = "Recupera o prontuário eletrônico completo, incluindo seus atendimentos registrados.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Prontuário localizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProntuarioResponseDTO.class))),
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
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR', 'PACIENTE', 'RESPONSAVEL')")
    public ResponseEntity<ProntuarioResponseDTO> obterProntuario(
            @Parameter(description = "Identificador único (UUID) do prontuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
        Prontuario prontuario = prontuarioService.obterProntuarioPorId(id);
        return ResponseEntity.ok(mapToResponseDTO(prontuario));
    }

    @Operation(
            summary = "Obter prontuário pelo ID do paciente",
            description = "Localiza o prontuário eletrônico vinculado diretamente ao UUID do paciente informado.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Prontuário do paciente retornado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProntuarioResponseDTO.class))),
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
                    description = "Prontuário não encontrado para o paciente informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR', 'PACIENTE', 'RESPONSAVEL')")
    public ResponseEntity<ProntuarioResponseDTO> obterProntuarioPorPacienteId(
            @Parameter(description = "Identificador único (UUID) do paciente", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID pacienteId) {
        Prontuario prontuario = prontuarioService.obterProntuarioPorPacienteId(pacienteId);
        return ResponseEntity.ok(mapToResponseDTO(prontuario));
    }

    @Operation(
            summary = "Obter histórico clínico completo do paciente",
            description = "Retorna a relação cronológica de todos os registros de atendimentos clínicos e evoluções do paciente.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Histórico clínico retornado com sucesso",
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
                    description = "Paciente não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/paciente/{pacienteId}/historico")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR', 'PACIENTE', 'RESPONSAVEL')")
    public ResponseEntity<List<RegistroAtendimentoResponseDTO>> obterHistoricoClinicoPaciente(
            @Parameter(description = "Identificador único (UUID) do paciente", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID pacienteId) {
        List<RegistroAtendimento> registros = prontuarioService.obterHistoricoClinicoPaciente(pacienteId);
        List<RegistroAtendimentoResponseDTO> response = registros.stream()
                .map(this::mapToRegistroResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    // Mappings Helpers
    private ProntuarioResponseDTO mapToResponseDTO(Prontuario prontuario) {
        List<RegistroAtendimentoResponseDTO> registros = prontuario.getRegistrosAtendimento().stream()
                .map(this::mapToRegistroResponseDTO)
                .toList();

        List<DocumentoProntuarioResponseDTO> documentos = prontuario.getDocumentos() != null
                ? prontuario.getDocumentos().stream().map(DocumentoProntuarioController::mapToResponseDTO).toList()
                : List.of();

        return new ProntuarioResponseDTO(
                prontuario.getId(),
                prontuario.getPaciente().getId(),
                prontuario.getPaciente().getUsuario() != null ? prontuario.getPaciente().getUsuario().getNome() : "N/A",
                prontuario.getObservacoesGerais(),
                registros,
                documentos
        );
    }

    private RegistroAtendimentoResponseDTO mapToRegistroResponseDTO(RegistroAtendimento registro) {
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