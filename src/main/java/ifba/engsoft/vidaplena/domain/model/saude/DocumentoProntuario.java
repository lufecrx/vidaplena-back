package ifba.engsoft.vidaplena.domain.model.saude;

import ifba.engsoft.vidaplena.infrastructure.auditing.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "documentos_prontuario")
public class DocumentoProntuario extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "O prontuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prontuario_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Prontuario prontuario;

    @NotNull(message = "O profissional responsável pelo envio é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registro_atendimento_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private RegistroAtendimento registroAtendimento;

    @NotNull(message = "O título do documento é obrigatório")
    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "O tipo do documento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 50, nullable = false)
    private TipoDocumento tipoDocumento;

    @NotNull(message = "O nome original do arquivo é obrigatório")
    @Column(name = "nome_original", nullable = false)
    private String nomeOriginal;

    @NotNull(message = "O nome do arquivo no armazenamento é obrigatório")
    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @NotNull(message = "O tipo de conteúdo (MIME type) é obrigatório")
    @Column(name = "tipo_conteudo", length = 100, nullable = false)
    private String tipoConteudo;

    @NotNull(message = "O tamanho do arquivo é obrigatório")
    @Column(name = "tamanho_bytes", nullable = false)
    private Long tamanhoBytes;

    @Column(name = "data_documento")
    private LocalDate dataDocumento;

    // Construtores
    public DocumentoProntuario() {}

    public DocumentoProntuario(Prontuario prontuario, Profissional profissional, String titulo,
                               TipoDocumento tipoDocumento, String nomeOriginal, String nomeArquivo,
                               String tipoConteudo, Long tamanhoBytes) {
        this.prontuario = prontuario;
        this.profissional = profissional;
        this.titulo = titulo;
        this.tipoDocumento = tipoDocumento;
        this.nomeOriginal = nomeOriginal;
        this.nomeArquivo = nomeArquivo;
        this.tipoConteudo = tipoConteudo;
        this.tamanhoBytes = tamanhoBytes;
        this.dataDocumento = LocalDate.now();
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Prontuario getProntuario() {
        return prontuario;
    }

    public void setProntuario(Prontuario prontuario) {
        this.prontuario = prontuario;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public RegistroAtendimento getRegistroAtendimento() {
        return registroAtendimento;
    }

    public void setRegistroAtendimento(RegistroAtendimento registroAtendimento) {
        this.registroAtendimento = registroAtendimento;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String getTipoConteudo() {
        return tipoConteudo;
    }

    public void setTipoConteudo(String tipoConteudo) {
        this.tipoConteudo = tipoConteudo;
    }

    public Long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public void setTamanhoBytes(Long tamanhoBytes) {
        this.tamanhoBytes = tamanhoBytes;
    }

    public LocalDate getDataDocumento() {
        return dataDocumento;
    }

    public void setDataDocumento(LocalDate dataDocumento) {
        this.dataDocumento = dataDocumento;
    }
}
