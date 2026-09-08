package ifba.engsoft.vidaplena.domain.model.prontuario;

import ifba.engsoft.vidaplena.domain.model.saude.DocumentoProntuario; // Ajuste o import se necessário para o pacote exato
import ifba.engsoft.vidaplena.infrastructure.auditing.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "documento_versoes")
public class DocumentoVersao extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "O ID do documento principal é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DocumentoProntuario documento;

    @NotNull(message = "O número da versão é obrigatório")
    @Column(name = "versao", nullable = false)
    private Integer versao;

    @NotNull(message = "O nome do arquivo é obrigatório")
    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @NotNull(message = "O tamanho em bytes é obrigatório")
    @Column(name = "tamanho_bytes", nullable = false)
    private Long tamanhoBytes;

    @NotNull(message = "O tipo de conteúdo é obrigatório")
    @Column(name = "tipo_conteudo", nullable = false)
    private String tipoConteudo;

    public DocumentoVersao() {}

    public DocumentoVersao(DocumentoProntuario documento, Integer versao, String nomeArquivo, Long tamanhoBytes, String tipoConteudo) {
        this.documento = documento;
        this.versao = versao;
        this.nomeArquivo = nomeArquivo;
        this.tamanhoBytes = tamanhoBytes;
        this.tipoConteudo = tipoConteudo;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public DocumentoProntuario getDocumento() { return documento; }
    public void setDocumento(DocumentoProntuario documento) { this.documento = documento; }

    public Integer getVersao() { return versao; }
    public void setVersao(Integer versao) { this.versao = versao; }

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }

    public Long getTamanhoBytes() { return tamanhoBytes; }
    public void setTamanhoBytes(Long tamanhoBytes) { this.tamanhoBytes = tamanhoBytes; }

    public String getTipoConteudo() { return tipoConteudo; }
    public void setTipoConteudo(String tipoConteudo) { this.tipoConteudo = tipoConteudo; }
}