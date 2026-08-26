package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.model.saude.*;
import ifba.engsoft.vidaplena.domain.repository.saude.DocumentoProntuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.ProfissionalRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.ProntuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.RegistroAtendimentoRepository;
import ifba.engsoft.vidaplena.domain.service.saude.exception.ProntuarioNotFoundException;
import ifba.engsoft.vidaplena.domain.service.saude.exception.RegraNegocioException;
import ifba.engsoft.vidaplena.infrastructure.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoProntuarioServiceTest {

    @Mock
    private DocumentoProntuarioRepository documentoProntuarioRepository;

    @Mock
    private ProntuarioRepository prontuarioRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private RegistroAtendimentoRepository registroAtendimentoRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private DocumentoProntuarioService documentoProntuarioService;

    private Prontuario prontuario;
    private Profissional profissional;
    private RegistroAtendimento registroAtendimento;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        prontuario = new Prontuario();
        prontuario.setId(UUID.randomUUID());

        profissional = new Profissional();
        profissional.setId(UUID.randomUUID());

        registroAtendimento = new RegistroAtendimento();
        registroAtendimento.setId(UUID.randomUUID());
        registroAtendimento.setProntuario(prontuario);

        mockFile = new MockMultipartFile(
                "arquivo",
                "laudo_ecg.pdf",
                "application/pdf",
                "Dados de teste do laudo".getBytes()
        );
    }

    @Test
    @DisplayName("Deve anexar documento ao prontuário com sucesso")
    void deveAnexarDocumentoComSucesso() {
        when(prontuarioRepository.findById(prontuario.getId())).thenReturn(Optional.of(prontuario));
        when(profissionalRepository.findById(profissional.getId())).thenReturn(Optional.of(profissional));
        when(fileStorageService.salvarArquivo(mockFile)).thenReturn("stored_laudo_ecg.pdf");
        when(documentoProntuarioRepository.save(any(DocumentoProntuario.class)))
                .thenAnswer(invocation -> {
                    DocumentoProntuario doc = invocation.getArgument(0);
                    doc.setId(UUID.randomUUID());
                    return doc;
                });

        DocumentoProntuario result = documentoProntuarioService.anexarDocumento(
                prontuario.getId(),
                profissional.getId(),
                null,
                "Eletrocardiograma de Repouso",
                "Exame normal",
                TipoDocumento.LAUDO,
                LocalDate.of(2026, 8, 20),
                mockFile
        );

        assertThat(result).isNotNull();
        assertThat(result.getTitulo()).isEqualTo("Eletrocardiograma de Repouso");
        assertThat(result.getTipoDocumento()).isEqualTo(TipoDocumento.LAUDO);
        assertThat(result.getNomeArquivo()).isEqualTo("stored_laudo_ecg.pdf");
        assertThat(result.getNomeOriginal()).isEqualTo("laudo_ecg.pdf");
        verify(fileStorageService, times(1)).salvarArquivo(mockFile);
        verify(documentoProntuarioRepository, times(1)).save(any(DocumentoProntuario.class));
    }

    @Test
    @DisplayName("Deve anexar documento vinculado a um atendimento clínico com sucesso")
    void deveAnexarDocumentoComRegistroAtendimento() {
        when(prontuarioRepository.findById(prontuario.getId())).thenReturn(Optional.of(prontuario));
        when(profissionalRepository.findById(profissional.getId())).thenReturn(Optional.of(profissional));
        when(registroAtendimentoRepository.findById(registroAtendimento.getId())).thenReturn(Optional.of(registroAtendimento));
        when(fileStorageService.salvarArquivo(mockFile)).thenReturn("stored_file.pdf");
        when(documentoProntuarioRepository.save(any(DocumentoProntuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentoProntuario result = documentoProntuarioService.anexarDocumento(
                prontuario.getId(),
                profissional.getId(),
                registroAtendimento.getId(),
                "Exame de Sangue",
                "Hemograma",
                TipoDocumento.EXAME_LABORATORIAL,
                LocalDate.now(),
                mockFile
        );

        assertThat(result.getRegistroAtendimento()).isEqualTo(registroAtendimento);
    }

    @Test
    @DisplayName("Deve falhar ao anexar documento se prontuário não for encontrado")
    void deveFalharSeProntuarioNaoEncontrado() {
        UUID prontuarioInexistente = UUID.randomUUID();
        when(prontuarioRepository.findById(prontuarioInexistente)).thenReturn(Optional.empty());

        assertThrows(ProntuarioNotFoundException.class, () ->
                documentoProntuarioService.anexarDocumento(
                        prontuarioInexistente,
                        profissional.getId(),
                        null,
                        "Título",
                        "Desc",
                        TipoDocumento.OUTROS,
                        LocalDate.now(),
                        mockFile
                ));
    }

    @Test
    @DisplayName("Deve falhar ao anexar se registro de atendimento pertencer a outro prontuário")
    void deveFalharSeRegistroPertenceAOutroProntuario() {
        Prontuario outroProntuario = new Prontuario();
        outroProntuario.setId(UUID.randomUUID());

        RegistroAtendimento registroDeOutro = new RegistroAtendimento();
        registroDeOutro.setId(UUID.randomUUID());
        registroDeOutro.setProntuario(outroProntuario);

        when(prontuarioRepository.findById(prontuario.getId())).thenReturn(Optional.of(prontuario));
        when(profissionalRepository.findById(profissional.getId())).thenReturn(Optional.of(profissional));
        when(registroAtendimentoRepository.findById(registroDeOutro.getId())).thenReturn(Optional.of(registroDeOutro));

        assertThrows(RegraNegocioException.class, () ->
                documentoProntuarioService.anexarDocumento(
                        prontuario.getId(),
                        profissional.getId(),
                        registroDeOutro.getId(),
                        "Título",
                        "Desc",
                        TipoDocumento.LAUDO,
                        LocalDate.now(),
                        mockFile
                ));
    }

    @Test
    @DisplayName("Deve listar documentos por prontuário com e sem filtro de tipo")
    void deveListarDocumentosPorProntuario() {
        when(prontuarioRepository.existsById(prontuario.getId())).thenReturn(true);
        when(documentoProntuarioRepository.findAllByProntuarioIdOrderByDataCriacaoDesc(prontuario.getId()))
                .thenReturn(List.of(new DocumentoProntuario()));
        when(documentoProntuarioRepository.findAllByProntuarioIdAndTipoDocumentoOrderByDataCriacaoDesc(prontuario.getId(), TipoDocumento.EXAME_IMAGEM))
                .thenReturn(List.of(new DocumentoProntuario()));

        List<DocumentoProntuario> todos = documentoProntuarioService.listarPorProntuario(prontuario.getId(), null);
        assertThat(todos).hasSize(1);

        List<DocumentoProntuario> imagens = documentoProntuarioService.listarPorProntuario(prontuario.getId(), TipoDocumento.EXAME_IMAGEM);
        assertThat(imagens).hasSize(1);
    }

    @Test
    @DisplayName("Deve carregar recurso de arquivo para download")
    void deveCarregarRecursoArquivo() {
        UUID docId = UUID.randomUUID();
        DocumentoProntuario doc = new DocumentoProntuario();
        doc.setId(docId);
        doc.setNomeArquivo("unique_ecg.pdf");

        when(documentoProntuarioRepository.findById(docId)).thenReturn(Optional.of(doc));
        Resource mockResource = new ByteArrayResource("bytes".getBytes());
        when(fileStorageService.carregarArquivoComoRecurso("unique_ecg.pdf")).thenReturn(mockResource);

        Resource result = documentoProntuarioService.carregarRecursoArquivo(docId);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar documento e arquivo físico com sucesso")
    void deveDeletarDocumento() {
        UUID docId = UUID.randomUUID();
        DocumentoProntuario doc = new DocumentoProntuario();
        doc.setId(docId);
        doc.setNomeArquivo("delete_me.pdf");

        when(documentoProntuarioRepository.findById(docId)).thenReturn(Optional.of(doc));

        documentoProntuarioService.deletarDocumento(docId);

        verify(fileStorageService, times(1)).deletarArquivo("delete_me.pdf");
        verify(documentoProntuarioRepository, times(1)).delete(doc);
    }
}
