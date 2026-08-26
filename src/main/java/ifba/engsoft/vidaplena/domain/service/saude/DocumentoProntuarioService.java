package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.model.saude.DocumentoProntuario;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import ifba.engsoft.vidaplena.domain.model.saude.Prontuario;
import ifba.engsoft.vidaplena.domain.model.saude.RegistroAtendimento;
import ifba.engsoft.vidaplena.domain.model.saude.TipoDocumento;
import ifba.engsoft.vidaplena.domain.repository.saude.DocumentoProntuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.ProfissionalRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.ProntuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.RegistroAtendimentoRepository;
import ifba.engsoft.vidaplena.domain.service.saude.exception.ProntuarioNotFoundException;
import ifba.engsoft.vidaplena.domain.service.saude.exception.RegraNegocioException;
import ifba.engsoft.vidaplena.infrastructure.storage.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class DocumentoProntuarioService {

    private final DocumentoProntuarioRepository documentoProntuarioRepository;
    private final ProntuarioRepository prontuarioRepository;
    private final ProfissionalRepository profissionalRepository;
    private final RegistroAtendimentoRepository registroAtendimentoRepository;
    private final FileStorageService fileStorageService;

    public DocumentoProntuarioService(
            DocumentoProntuarioRepository documentoProntuarioRepository,
            ProntuarioRepository prontuarioRepository,
            ProfissionalRepository profissionalRepository,
            RegistroAtendimentoRepository registroAtendimentoRepository,
            FileStorageService fileStorageService) {
        this.documentoProntuarioRepository = documentoProntuarioRepository;
        this.prontuarioRepository = prontuarioRepository;
        this.profissionalRepository = profissionalRepository;
        this.registroAtendimentoRepository = registroAtendimentoRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Anexa um novo documento ou imagem ao prontuário do paciente.
     */
    @Transactional
    public DocumentoProntuario anexarDocumento(
            UUID prontuarioId,
            UUID profissionalId,
            UUID registroAtendimentoId,
            String titulo,
            String descricao,
            TipoDocumento tipoDocumento,
            LocalDate dataDocumento,
            MultipartFile arquivo) {

        if (prontuarioId == null) {
            throw new RegraNegocioException("O ID do prontuário é obrigatório.");
        }
        if (profissionalId == null) {
            throw new RegraNegocioException("O ID do profissional é obrigatório.");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new RegraNegocioException("O título do documento é obrigatório.");
        }
        if (tipoDocumento == null) {
            throw new RegraNegocioException("O tipo de documento é obrigatório.");
        }
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("O arquivo a ser anexado é obrigatório e não pode estar vazio.");
        }

        Prontuario prontuario = prontuarioRepository.findById(prontuarioId)
                .orElseThrow(() -> new ProntuarioNotFoundException("Prontuário não encontrado para o ID informado: " + prontuarioId));

        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RegraNegocioException("Profissional não encontrado para o ID informado: " + profissionalId));

        RegistroAtendimento registroAtendimento = null;
        if (registroAtendimentoId != null) {
            registroAtendimento = registroAtendimentoRepository.findById(registroAtendimentoId)
                    .orElseThrow(() -> new RegraNegocioException("Registro de atendimento não encontrado para o ID: " + registroAtendimentoId));

            if (!registroAtendimento.getProntuario().getId().equals(prontuarioId)) {
                throw new RegraNegocioException("O registro de atendimento informado não pertence ao prontuário indicado.");
            }
        }

        // Salvar arquivo no armazenamento local/seguro
        String nomeArquivoSalvo = fileStorageService.salvarArquivo(arquivo);
        String nomeOriginal = StringUtils.cleanPath(Objects.requireNonNullElse(arquivo.getOriginalFilename(), "documento"));
        String contentType = arquivo.getContentType() != null ? arquivo.getContentType() : "application/octet-stream";
        long tamanhoBytes = arquivo.getSize();

        DocumentoProntuario documento = new DocumentoProntuario();
        documento.setProntuario(prontuario);
        documento.setProfissional(profissional);
        documento.setRegistroAtendimento(registroAtendimento);
        documento.setTitulo(titulo.trim());
        documento.setDescricao(descricao != null ? descricao.trim() : null);
        documento.setTipoDocumento(tipoDocumento);
        documento.setNomeOriginal(nomeOriginal);
        documento.setNomeArquivo(nomeArquivoSalvo);
        documento.setTipoConteudo(contentType);
        documento.setTamanhoBytes(tamanhoBytes);
        documento.setDataDocumento(dataDocumento != null ? dataDocumento : LocalDate.now());

        return documentoProntuarioRepository.save(documento);
    }

    /**
     * Recupera os metadados de um documento anexado pelo seu ID.
     */
    public DocumentoProntuario obterDocumentoPorId(UUID id) {
        return documentoProntuarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Documento não encontrado para o ID: " + id));
    }

    /**
     * Lista todos os documentos anexados a um prontuário específico, com filtro opcional por categoria.
     */
    public List<DocumentoProntuario> listarPorProntuario(UUID prontuarioId, TipoDocumento tipoDocumento) {
        if (!prontuarioRepository.existsById(prontuarioId)) {
            throw new ProntuarioNotFoundException("Prontuário não encontrado para o ID informado: " + prontuarioId);
        }

        if (tipoDocumento != null) {
            return documentoProntuarioRepository.findAllByProntuarioIdAndTipoDocumentoOrderByDataCriacaoDesc(prontuarioId, tipoDocumento);
        }
        return documentoProntuarioRepository.findAllByProntuarioIdOrderByDataCriacaoDesc(prontuarioId);
    }

    /**
     * Lista documentos anexados especificamente a um atendimento clínico.
     */
    public List<DocumentoProntuario> listarPorRegistroAtendimento(UUID registroAtendimentoId) {
        if (!registroAtendimentoRepository.existsById(registroAtendimentoId)) {
            throw new RegraNegocioException("Registro de atendimento não encontrado para o ID informado: " + registroAtendimentoId);
        }
        return documentoProntuarioRepository.findAllByRegistroAtendimentoIdOrderByDataCriacaoDesc(registroAtendimentoId);
    }

    /**
     * Carrega o recurso binário do arquivo para download ou visualização inline.
     */
    public Resource carregarRecursoArquivo(UUID documentoId) {
        DocumentoProntuario documento = obterDocumentoPorId(documentoId);
        return fileStorageService.carregarArquivoComoRecurso(documento.getNomeArquivo());
    }

    /**
     * Exclui um documento anexado e remove seu arquivo do armazenamento.
     */
    @Transactional
    public void deletarDocumento(UUID id) {
        DocumentoProntuario documento = obterDocumentoPorId(id);
        fileStorageService.deletarArquivo(documento.getNomeArquivo());
        documentoProntuarioRepository.delete(documento);
    }
}
