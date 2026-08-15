package ifba.engsoft.vidaplena.domain.model.saude;

import ifba.engsoft.vidaplena.infrastructure.auditing.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "registros_atendimento")
public class RegistroAtendimento extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "O prontuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prontuario_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Prontuario prontuario;

    @NotNull(message = "O profissional é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Profissional profissional;

    @NotNull(message = "O agendamento é obrigatório")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Agendamento agendamento;

    @NotNull(message = "A data de registro é obrigatória")
    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    @Column(name = "sintomas_relatados", columnDefinition = "TEXT")
    private String sintomasRelatados;

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "prescricao_medica", columnDefinition = "TEXT")
    private String prescricaoMedica;

    @Column(name = "prescricao_enfermagem", columnDefinition = "TEXT")
    private String prescricaoEnfermagem;

    @Column(name = "notas_clinicas", columnDefinition = "TEXT")
    private String notasClinicas;

    @Column(name = "finalizado", nullable = false)
    private boolean finalizado = false;

    @OneToMany(mappedBy = "registroAtendimento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NotaRetificacao> notasRetificacao = new ArrayList<>();

    // Construtores
    public RegistroAtendimento() {}

    public RegistroAtendimento(Prontuario prontuario, Profissional profissional, Agendamento agendamento) {
        this.prontuario = prontuario;
        this.profissional = profissional;
        this.agendamento = agendamento;
        this.dataRegistro = LocalDateTime.now();
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

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public String getSintomasRelatados() {
        return sintomasRelatados;
    }

    public void setSintomasRelatados(String sintomasRelatados) {
        this.sintomasRelatados = sintomasRelatados;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getPrescricaoMedica() {
        return prescricaoMedica;
    }

    public void setPrescricaoMedica(String prescricaoMedica) {
        this.prescricaoMedica = prescricaoMedica;
    }

    public String getPrescricaoEnfermagem() {
        return prescricaoEnfermagem;
    }

    public void setPrescricaoEnfermagem(String prescricaoEnfermagem) {
        this.prescricaoEnfermagem = prescricaoEnfermagem;
    }

    public String getNotasClinicas() {
        return notasClinicas;
    }

    public void setNotasClinicas(String notasClinicas) {
        this.notasClinicas = notasClinicas;
    }

    public boolean isFinalizado() {
        return finalizado;
    }

    public void setFinalizado(boolean finalizado) {
        this.finalizado = finalizado;
    }

    public List<NotaRetificacao> getNotasRetificacao() {
        return notasRetificacao;
    }

    public void setNotasRetificacao(List<NotaRetificacao> notasRetificacao) {
        this.notasRetificacao = notasRetificacao;
    }

    public void adicionarNotaRetificacao(NotaRetificacao nota) {
        this.notasRetificacao.add(nota);
        nota.setRegistroAtendimento(this);
    }
}