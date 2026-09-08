package ifba.engsoft.vidaplena.domain.service.prontuario;

import ifba.engsoft.vidaplena.domain.dto.prontuario.DocumentoVersaoResponse;
import ifba.engsoft.vidaplena.domain.model.saude.DocumentoProntuario;
import ifba.engsoft.vidaplena.domain.model.prontuario.DocumentoVersao;
import ifba.engsoft.vidaplena.domain.repository.DocumentoVersaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentoVersaoService {

    private final DocumentoVersaoRepository documentoVersaoRepository;

    public DocumentoVersaoService(DocumentoVersaoRepository documentoVersaoRepository) {
        this.documentoVersaoRepository = documentoVersaoRepository;
    }

    @Transactional
    public void registrarVersao(DocumentoProntuario documento, int versaoNumero) {
        DocumentoVersao versao = new DocumentoVersao(
            documento,
            versaoNumero,
            documento.getNomeArquivo(),
            documento.getTamanhoBytes(),
            documento.getTipoConteudo()
        );
        documentoVersaoRepository.save(versao);
    }

    @Transactional(readOnly = true)
    public List<DocumentoVersaoResponse> listarVersoesPorDocumento(UUID documentoId) {
        List<DocumentoVersao> versoes = documentoVersaoRepository.findByDocumentoIdOrderByVersaoDesc(documentoId);
        return versoes.stream()
                .map(DocumentoVersaoResponse::from)
                .collect(Collectors.toList());
    }
}