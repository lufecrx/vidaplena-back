package ifba.engsoft.vidaplena.infrastructure.storage;

import ifba.engsoft.vidaplena.domain.service.saude.exception.RegraNegocioException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private Path uploadLocation;
    private static final long MAX_FILE_SIZE_BYTES = 25 * 1024 * 1024; // 25MB

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "png", "jpg", "jpeg", "webp", "gif", "bmp", "tiff", "txt", "csv",
            "doc", "docx", "xls", "xlsx", "dcm", "dicom"
    );

    public FileStorageService(@Value("${app.storage.upload-dir:./uploads/documentos}") String uploadDir) {
        this.uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.uploadLocation);
            log.info("Diretório de armazenamento de anexos inicializado em: {}", this.uploadLocation);
        } catch (Exception ex) {
            log.warn("Não foi possível inicializar o diretório configurado de upload '{}': {}. Utilizando diretório temporário do sistema como fallback.", this.uploadLocation, ex.getMessage());
            try {
                this.uploadLocation = Paths.get(System.getProperty("java.io.tmpdir"), "vidaplena-uploads").toAbsolutePath().normalize();
                Files.createDirectories(this.uploadLocation);
                log.info("Diretório de fallback de uploads inicializado em: {}", this.uploadLocation);
            } catch (Exception fallbackEx) {
                log.error("Falha ao criar diretório de fallback para uploads: {}", fallbackEx.getMessage());
            }
        }
    }

    /**
     * Salva o arquivo multipart no sistema de arquivos local com nome único e seguro.
     *
     * @param file arquivo enviado
     * @return nome do arquivo gerado e salvo
     */
    public String salvarArquivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RegraNegocioException("O arquivo enviado não pode estar vazio.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new RegraNegocioException("O arquivo excede o tamanho máximo permitido de 25MB.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "documento"));

        if (originalFilename.contains("..")) {
            throw new RegraNegocioException("Nome de arquivo inválido contendo sequência de escape de caminho: " + originalFilename);
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RegraNegocioException("Extensão de arquivo não permitida: ." + extension + ". Extensões aceitas: PDF, imagens (PNG, JPG, WEBP), laudos (DOCX, TXT) e DICOM.");
        }

        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueFilename = UUID.randomUUID() + "_" + sanitizedFilename;

        try {
            if (!Files.exists(this.uploadLocation)) {
                Files.createDirectories(this.uploadLocation);
            }
            Path targetLocation = this.uploadLocation.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Arquivo armazenado com sucesso: {}", targetLocation);
            return uniqueFilename;
        } catch (IOException ex) {
            throw new RegraNegocioException("Falha ao salvar o arquivo no armazenamento: " + ex.getMessage());
        }
    }

    /**
     * Carrega o arquivo armazenado como Spring Resource para streaming/download.
     *
     * @param nomeArquivo nome físico do arquivo
     * @return Resource do arquivo
     */
    public Resource carregarArquivoComoRecurso(String nomeArquivo) {
        try {
            Path filePath = this.uploadLocation.resolve(nomeArquivo).normalize();
            if (!filePath.startsWith(this.uploadLocation)) {
                throw new RegraNegocioException("Acesso a caminho de arquivo não autorizado.");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RegraNegocioException("Arquivo não encontrado ou inacessível no armazenamento: " + nomeArquivo);
            }
        } catch (MalformedURLException ex) {
            throw new RegraNegocioException("URL de arquivo malformada: " + ex.getMessage());
        }
    }

    /**
     * Remove o arquivo do sistema de arquivos local.
     *
     * @param nomeArquivo nome físico do arquivo a ser excluído
     */
    public void deletarArquivo(String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.isBlank()) {
            return;
        }
        try {
            Path filePath = this.uploadLocation.resolve(nomeArquivo).normalize();
            if (filePath.startsWith(this.uploadLocation)) {
                Files.deleteIfExists(filePath);
                log.info("Arquivo excluído do armazenamento: {}", filePath);
            }
        } catch (IOException ex) {
            log.warn("Não foi possível excluir o arquivo {} do disco: {}", nomeArquivo, ex.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1 || dotIndex == filename.length() - 1) ? "" : filename.substring(dotIndex + 1);
    }

    public Path getUploadLocation() {
        return uploadLocation;
    }
}
