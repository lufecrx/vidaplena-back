package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.DocumentoProntuarioResponseDTO;
import ifba.engsoft.vidaplena.domain.model.saude.DocumentoProntuario;
import ifba.engsoft.vidaplena.domain.model.saude.TipoDocumento;
import ifba.engsoft.vidaplena.domain.service.saude.DocumentoProntuarioService;
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
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/prontuarios", "/api/prontuarios"})
@CrossOrigin(origins = "*")
@Tag(name = "Documentos e Laudos do Prontuário", description = "Gerenciamento de anexos, laudos médicos, exames laboratoriais e imagens clínicas vinculados ao prontuário eletrônico")
@SecurityRequirement(name = "bearerAuth")
public class DocumentoProntuarioController {

    private final DocumentoProntuarioService documentoProntuarioService;

    public DocumentoProntuarioController(DocumentoProntuarioService documentoProntuarioService) {
        this.documentoProntuarioService = documentoProntuarioService;
    }

    @Operation(
            summary = "Anexar documento ou imagem ao prontuário",
            description = "Realiza o upload de arquivos de exames, laudos ou imagens clínicas (PDF, PNG, JPG, WEBP, DICOM, etc.) vinculado ao prontuário.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Documento anexado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentoProntuarioResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos, arquivo vazio ou extensão não permitida",
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
                    responseCode = "404",
                    description = "Prontuário ou profissional não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping(value = "/{prontuarioId}/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR')")
    public ResponseEntity<DocumentoProntuarioResponseDTO> anexarDocumento(
            @Parameter(description = "Identificador único (UUID) do prontuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID prontuarioId,

            @Parameter(description = "Arquivo binário (PDF, PNG, JPG, DICOM, etc.)")
            @RequestParam("arquivo") MultipartFile arquivo,

            @Parameter(description = "Identificador único (UUID) do profissional responsável pelo anexo", example = "890e4567-e89b-12d3-a456-426614174000")
            @RequestParam("profissionalId") UUID profissionalId,

            @Parameter(description = "Título ou nome descritivo do documento", example = "Eletrocardiograma de Repouso")
            @RequestParam("titulo") String titulo,

            @Parameter(description = "Descrição ou observações clínicas do documento", example = "Paciente assintomático durante o traçado.")
            @RequestParam(value = "descricao", required = false) String descricao,

            @Parameter(description = "Categoria do documento", example = "EXAME_IMAGEM")
            @RequestParam("tipoDocumento") TipoDocumento tipoDocumento,

            @Parameter(description = "Data do exame ou emissão do documento (AAAA-MM-DD)", example = "2026-08-20")
            @RequestParam(value = "dataDocumento", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDocumento,

            @Parameter(description = "Identificador do registro de atendimento associado (opcional)", example = "111e4567-e89b-12d3-a456-426614174000")
            @RequestParam(value = "registroAtendimentoId", required = false) UUID registroAtendimentoId) {

        DocumentoProntuario doc = documentoProntuarioService.anexarDocumento(
                prontuarioId,
                profissionalId,
                registroAtendimentoId,
                titulo,
                descricao,
                tipoDocumento,
                dataDocumento,
                arquivo
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponseDTO(doc));
    }

    @Operation(
            summary = "Listar documentos anexados ao prontuário",
            description = "Recupera todos os laudos, exames e documentos vinculados a um prontuário eletrônico.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de documentos retornada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DocumentoProntuarioResponseDTO.class)))),
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
                    description = "Prontuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{prontuarioId}/documentos")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR', 'PACIENTE', 'RESPONSAVEL')")
    public ResponseEntity<List<DocumentoProntuarioResponseDTO>> listarDocumentos(
            @Parameter(description = "Identificador único (UUID) do prontuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID prontuarioId,

            @Parameter(description = "Filtro opcional por tipo de documento", example = "EXAME_LABORATORIAL")
            @RequestParam(value = "tipoDocumento", required = false) TipoDocumento tipoDocumento) {

        List<DocumentoProntuario> documentos = documentoProntuarioService.listarPorProntuario(prontuarioId, tipoDocumento);
        List<DocumentoProntuarioResponseDTO> response = documentos.stream()
                .map(DocumentoProntuarioController::mapToResponseDTO)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obter metadados de documento anexado por ID",
            description = "Recupera as informações descritivas e metadados de um documento específico anexado ao prontuário.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Metadados do documento retornados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentoProntuarioResponseDTO.class))),
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
                    description = "Documento não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/documentos/{documentoId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR', 'PACIENTE', 'RESPONSAVEL')")
    public ResponseEntity<DocumentoProntuarioResponseDTO> obterDocumentoPorId(
            @Parameter(description = "Identificador único (UUID) do documento", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID documentoId) {

        DocumentoProntuario doc = documentoProntuarioService.obterDocumentoPorId(documentoId);
        return ResponseEntity.ok(mapToResponseDTO(doc));
    }

    @Operation(
            summary = "Download ou visualização do arquivo do documento",
            description = "Transmite o fluxo binário do arquivo (PDF, imagem, etc.) para download ou renderização no navegador.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Arquivo transmitido com sucesso"),
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
                    description = "Documento ou arquivo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/documentos/{documentoId}/download")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR', 'PACIENTE', 'RESPONSAVEL')")
    public ResponseEntity<Resource> baixarArquivo(
            @Parameter(description = "Identificador único (UUID) do documento a ser baixado", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID documentoId) {

        DocumentoProntuario doc = documentoProntuarioService.obterDocumentoPorId(documentoId);
        Resource recurso = documentoProntuarioService.carregarRecursoArquivo(documentoId);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(doc.getTipoConteudo());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getNomeOriginal() + "\"")
                .body(recurso);
    }

    @Operation(
            summary = "Excluir documento anexado",
            description = "Exclui o registro do documento do prontuário e remove o arquivo físico correspondente do armazenamento.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Documento excluído com sucesso (sem conteúdo)"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado / Token JWT ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - Requer perfil clínico ou administrador",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Documento não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/documentos/{documentoId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR')")
    public ResponseEntity<Void> deletarDocumento(
            @Parameter(description = "Identificador único (UUID) do documento a ser excluído", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID documentoId) {

        documentoProntuarioService.deletarDocumento(documentoId);
        return ResponseEntity.noContent().build();
    }

    // Helper mapping
    public static DocumentoProntuarioResponseDTO mapToResponseDTO(DocumentoProntuario doc) {
        String downloadUrl = "/api/v1/prontuarios/documentos/" + doc.getId() + "/download";
        String profissionalNome = (doc.getProfissional() != null && doc.getProfissional().getUsuario() != null)
                ? doc.getProfissional().getUsuario().getNome()
                : "N/A";
        String profissionalRegistro = (doc.getProfissional() != null)
                ? doc.getProfissional().getRegistroConselho()
                : "N/A";

        return new DocumentoProntuarioResponseDTO(
                doc.getId(),
                doc.getProntuario() != null ? doc.getProntuario().getId() : null,
                doc.getProfissional() != null ? doc.getProfissional().getId() : null,
                profissionalNome,
                profissionalRegistro,
                doc.getRegistroAtendimento() != null ? doc.getRegistroAtendimento().getId() : null,
                doc.getTitulo(),
                doc.getDescricao(),
                doc.getTipoDocumento(),
                doc.getNomeOriginal(),
                doc.getTipoConteudo(),
                doc.getTamanhoBytes(),
                doc.getDataDocumento(),
                doc.getDataCriacao(),
                downloadUrl
        );
    }
}
