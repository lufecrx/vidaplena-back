package ifba.engsoft.vidaplena.domain.model.saude;

import ifba.engsoft.vidaplena.infrastructure.auditing.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "prontuarios")
public class Prontuario extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "O ID do paciente é obrigatório")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Paciente paciente;

    @Column(name = "observacoes_gerais", length = 2000)
    private String observacoesGerais;

    @OneToMany(mappedBy = "prontuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RegistroAtendimento> registrosAtendimento = new ArrayList<>();

    @OneToMany(mappedBy = "prontuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentoProntuario> documentos = new ArrayList<>();

    // Construtores
    public Prontuario() {}

    public Prontuario(Paciente paciente) {
        this.paciente = paciente;
    }

    public Prontuario(Paciente paciente, String observacoesGerais) {
        this.paciente = paciente;
        this.observacoesGerais = observacoesGerais;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public String getObservacoesGerais() {
        return observacoesGerais;
    }

    public void setObservacoesGerais(String observacoesGerais) {
        this.observacoesGerais = observacoesGerais;
    }

    public List<RegistroAtendimento> getRegistrosAtendimento() {
        return registrosAtendimento;
    }

    public void setRegistrosAtendimento(List<RegistroAtendimento> registrosAtendimento) {
        this.registrosAtendimento = registrosAtendimento;
    }

    public void adicionarRegistro(RegistroAtendimento registro) {
        this.registrosAtendimento.add(registro);
        registro.setProntuario(this);
    }

    public List<DocumentoProntuario> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<DocumentoProntuario> documentos) {
        this.documentos = documentos;
    }

    public void adicionarDocumento(DocumentoProntuario documento) {
        this.documentos.add(documento);
        documento.setProntuario(this);
    }
}