package ifba.engsoft.vidaplena.domain.model.saude;

import ifba.engsoft.vidaplena.infrastructure.auditing.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notas_retificacao")
public class NotaRetificacao extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "O registro de atendimento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registro_atendimento_id", nullable = false)
    private RegistroAtendimento registroAtendimento;

    @NotNull(message = "O profissional é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @NotNull(message = "A data de registro é obrigatória")
    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    @NotNull(message = "O texto de retificação é obrigatório")
    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    // Construtores
    public NotaRetificacao() {}

    public NotaRetificacao(RegistroAtendimento registroAtendimento, Profissional profissional, String texto) {
        this.registroAtendimento = registroAtendimento;
        this.profissional = profissional;
        this.texto = texto;
        this.dataRegistro = LocalDateTime.now();
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RegistroAtendimento getRegistroAtendimento() {
        return registroAtendimento;
    }

    public void setRegistroAtendimento(RegistroAtendimento registroAtendimento) {
        this.registroAtendimento = registroAtendimento;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
