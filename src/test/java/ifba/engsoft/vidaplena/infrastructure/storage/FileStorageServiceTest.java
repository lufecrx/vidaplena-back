package ifba.engsoft.vidaplena.infrastructure.storage;

import ifba.engsoft.vidaplena.domain.service.saude.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.init();
    }

    @Test
    @DisplayName("Deve salvar arquivo PDF válido com sucesso")
    void deveSalvarArquivoPdfComSucesso() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "laudo_exame.pdf",
                "application/pdf",
                "Conteudo do laudo medico em PDF".getBytes()
        );

        String savedName = fileStorageService.salvarArquivo(file);

        assertThat(savedName).isNotNull().contains("laudo_exame.pdf");
        assertThat(Files.exists(tempDir.resolve(savedName))).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar arquivo vazio")
    void deveRejeitarArquivoVazio() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "vazio.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(RegraNegocioException.class, () -> fileStorageService.salvarArquivo(file));
    }

    @Test
    @DisplayName("Deve rejeitar extensão não permitida (.exe, .sh)")
    void deveRejeitarExtensaoNaoPermitida() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "malware.exe",
                "application/octet-stream",
                "binario".getBytes()
        );

        assertThrows(RegraNegocioException.class, () -> fileStorageService.salvarArquivo(file));
    }

    @Test
    @DisplayName("Deve rejeitar tentativa de path traversal")
    void deveRejeitarPathTraversal() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "../arquivo_malicioso.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        assertThrows(RegraNegocioException.class, () -> fileStorageService.salvarArquivo(file));
    }

    @Test
    @DisplayName("Deve carregar recurso de arquivo salvo com sucesso")
    void deveCarregarRecursoComSucesso() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "radiografia.png",
                "image/png",
                "imagem PNG".getBytes()
        );

        String savedName = fileStorageService.salvarArquivo(file);
        Resource resource = fileStorageService.carregarArquivoComoRecurso(savedName);

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar exceção ao carregar arquivo inexistente")
    void deveLancarExcecaoAoCarregarInexistente() {
        assertThrows(RegraNegocioException.class, () -> fileStorageService.carregarArquivoComoRecurso("arquivo_inexistente.pdf"));
    }

    @Test
    @DisplayName("Deve excluir arquivo salvo com sucesso")
    void deveExcluirArquivoComSucesso() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "excluir.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        String savedName = fileStorageService.salvarArquivo(file);
        assertThat(Files.exists(tempDir.resolve(savedName))).isTrue();

        fileStorageService.deletarArquivo(savedName);
        assertThat(Files.exists(tempDir.resolve(savedName))).isFalse();
    }
}
