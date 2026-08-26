package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.model.saude.DocumentoProntuario;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import ifba.engsoft.vidaplena.domain.model.saude.Prontuario;
import ifba.engsoft.vidaplena.domain.model.saude.TipoDocumento;
import ifba.engsoft.vidaplena.domain.service.saude.DocumentoProntuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentoProntuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentoProntuarioService documentoProntuarioService;

    private static final String BASE_URL = "/api/v1/prontuarios";

    @Test
    @DisplayName("Deve negar acesso anônimo sem autenticação (HTTP 401)")
    void deveNegarAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID() + "/documentos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve negar upload de documento para perfil PACIENTE (HTTP 403)")
    void deveNegarUploadParaPaciente() throws Exception {
        UUID prontuarioId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("arquivo", "laudo.pdf", "application/pdf", "conteudo".getBytes());

        mockMvc.perform(multipart(BASE_URL + "/{prontuarioId}/documentos", prontuarioId)
                        .file(file)
                        .param("profissionalId", UUID.randomUUID().toString())
                        .param("titulo", "Laudo ECG")
                        .param("tipoDocumento", "LAUDO"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve permitir upload de documento para perfil MEDICO (HTTP 201)")
    void devePermitirUploadParaMedico() throws Exception {
        UUID prontuarioId = UUID.randomUUID();
        UUID profissionalId = UUID.randomUUID();
        UUID docId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile("arquivo", "laudo_ecg.pdf", "application/pdf", "conteudo pdf".getBytes());

        Prontuario prontuario = new Prontuario();
        prontuario.setId(prontuarioId);

        Profissional profissional = new Profissional();
        profissional.setId(profissionalId);

        DocumentoProntuario doc = new DocumentoProntuario();
        doc.setId(docId);
        doc.setProntuario(prontuario);
        doc.setProfissional(profissional);
        doc.setTitulo("Laudo de Eletrocardiograma");
        doc.setTipoDocumento(TipoDocumento.LAUDO);
        doc.setNomeOriginal("laudo_ecg.pdf");
        doc.setNomeArquivo("unique_laudo_ecg.pdf");
        doc.setTipoConteudo("application/pdf");
        doc.setTamanhoBytes(1024L);
        doc.setDataDocumento(LocalDate.of(2026, 8, 20));

        when(documentoProntuarioService.anexarDocumento(
                eq(prontuarioId), eq(profissionalId), isNull(), eq("Laudo de Eletrocardiograma"),
                any(), eq(TipoDocumento.LAUDO), any(), any()))
                .thenReturn(doc);

        mockMvc.perform(multipart(BASE_URL + "/{prontuarioId}/documentos", prontuarioId)
                        .file(file)
                        .param("profissionalId", profissionalId.toString())
                        .param("titulo", "Laudo de Eletrocardiograma")
                        .param("tipoDocumento", "LAUDO"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(docId.toString()))
                .andExpect(jsonPath("$.titulo").value("Laudo de Eletrocardiograma"))
                .andExpect(jsonPath("$.tipoDocumento").value("LAUDO"))
                .andExpect(jsonPath("$.downloadUrl").value("/api/v1/prontuarios/documentos/" + docId + "/download"));

        verify(documentoProntuarioService, times(1)).anexarDocumento(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve permitir listagem de documentos para perfil PACIENTE (HTTP 200)")
    void devePermitirListagemParaPaciente() throws Exception {
        UUID prontuarioId = UUID.randomUUID();
        when(documentoProntuarioService.listarPorProntuario(eq(prontuarioId), any()))
                .thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/{prontuarioId}/documentos", prontuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(documentoProntuarioService, times(1)).listarPorProntuario(eq(prontuarioId), isNull());
    }

    @Test
    @WithMockUser(roles = "NUTRICIONISTA")
    @DisplayName("Deve obter detalhes do documento por ID (HTTP 200)")
    void deveObterDocumentoPorId() throws Exception {
        UUID docId = UUID.randomUUID();
        DocumentoProntuario doc = new DocumentoProntuario();
        doc.setId(docId);
        doc.setTitulo("Avaliação Bioimpedância");
        doc.setTipoDocumento(TipoDocumento.EXAME_LABORATORIAL);
        doc.setNomeOriginal("bioimpedancia.pdf");
        doc.setTipoConteudo("application/pdf");
        doc.setTamanhoBytes(2048L);

        when(documentoProntuarioService.obterDocumentoPorId(docId)).thenReturn(doc);

        mockMvc.perform(get(BASE_URL + "/documentos/{documentoId}", docId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(docId.toString()))
                .andExpect(jsonPath("$.titulo").value("Avaliação Bioimpedância"));

        verify(documentoProntuarioService, times(1)).obterDocumentoPorId(docId);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve baixar arquivo do documento com headers corretos (HTTP 200)")
    void deveBaixarArquivo() throws Exception {
        UUID docId = UUID.randomUUID();
        DocumentoProntuario doc = new DocumentoProntuario();
        doc.setId(docId);
        doc.setNomeOriginal("hemograma.pdf");
        doc.setTipoConteudo("application/pdf");

        byte[] content = "PDF-Content".getBytes();
        Resource resource = new ByteArrayResource(content);

        when(documentoProntuarioService.obterDocumentoPorId(docId)).thenReturn(doc);
        when(documentoProntuarioService.carregarRecursoArquivo(docId)).thenReturn(resource);

        mockMvc.perform(get(BASE_URL + "/documentos/{documentoId}/download", docId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"hemograma.pdf\""))
                .andExpect(content().bytes(content));

        verify(documentoProntuarioService, times(1)).carregarRecursoArquivo(docId);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve permitir exclusão de documento para perfil MEDICO (HTTP 204)")
    void deveExcluirDocumentoParaMedico() throws Exception {
        UUID docId = UUID.randomUUID();
        doNothing().when(documentoProntuarioService).deletarDocumento(docId);

        mockMvc.perform(delete(BASE_URL + "/documentos/{documentoId}", docId))
                .andExpect(status().isNoContent());

        verify(documentoProntuarioService, times(1)).deletarDocumento(docId);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve negar exclusão de documento para perfil PACIENTE (HTTP 403)")
    void deveNegarExclusaoParaPaciente() throws Exception {
        UUID docId = UUID.randomUUID();

        mockMvc.perform(delete(BASE_URL + "/documentos/{documentoId}", docId))
                .andExpect(status().isForbidden());

        verify(documentoProntuarioService, never()).deletarDocumento(any());
    }
}
