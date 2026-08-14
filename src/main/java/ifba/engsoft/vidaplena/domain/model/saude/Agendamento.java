package ifba.engsoft.vidaplena.domain.model.saude;

import ifba.engsoft.vidaplena.domain.model.organizacao.Clinica;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    @NotNull
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    @NotNull
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinica_id")
    private Clinica clinica;

    @Column(name = "plano_corporativo_id")
    private UUID planoCorporativoId;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Column(name = "data_hora_inicio")
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim")
    private LocalDateTime dataHoraFim;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusAgendamento status;

    @Column(name = "tipo_atendimento")
    @Enumerated(EnumType.STRING)
    private TipoAtendimento tipoAtendimento;

    @Column(name = "motivo_consulta")
    private String motivoConsulta;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    @Version
    private Long version;

    // Construtores
    public Agendamento() {}

    public Agendamento(Paciente paciente, Profissional profissional, LocalDateTime dataHoraInicio,
                       LocalDateTime dataHoraFim, StatusAgendamento status, String motivoConsulta) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.dataHora = dataHoraInicio;
        this.status = status;
        this.motivoConsulta = motivoConsulta;
        this.observacoes = motivoConsulta;
    }

    public Agendamento(Paciente paciente, Profissional profissional, Clinica clinica,
                       UUID planoCorporativoId, LocalDateTime dataHora, StatusAgendamento status,
                       TipoAtendimento tipoAtendimento, String observacoes) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.clinica = clinica;
        this.planoCorporativoId = planoCorporativoId;
        this.dataHora = dataHora;
        this.dataHoraInicio = dataHora;
        this.dataHoraFim = dataHora != null ? dataHora.plusHours(1) : null;
        this.status = status;
        this.tipoAtendimento = tipoAtendimento;
        this.observacoes = observacoes;
        this.motivoConsulta = observacoes;
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

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Clinica getClinica() {
        return clinica;
    }

    public void setClinica(Clinica clinica) {
        this.clinica = clinica;
    }

    public UUID getPlanoCorporativoId() {
        return planoCorporativoId;
    }

    public void setPlanoCorporativoId(UUID planoCorporativoId) {
        this.planoCorporativoId = planoCorporativoId;
    }

    public LocalDateTime getDataHora() {
        return dataHora != null ? dataHora : dataHoraInicio;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
        if (this.dataHoraInicio == null) {
            this.dataHoraInicio = dataHora;
        }
        if (this.dataHoraFim == null && dataHora != null) {
            this.dataHoraFim = dataHora.plusHours(1);
        }
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio != null ? dataHoraInicio : dataHora;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
        if (this.dataHora == null) {
            this.dataHora = dataHoraInicio;
        }
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public TipoAtendimento getTipoAtendimento() {
        return tipoAtendimento;
    }

    public void setTipoAtendimento(TipoAtendimento tipoAtendimento) {
        this.tipoAtendimento = tipoAtendimento;
    }

    public String getMotivoConsulta() {
        return motivoConsulta != null ? motivoConsulta : observacoes;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
        if (this.observacoes == null) {
            this.observacoes = motivoConsulta;
        }
    }

    public String getObservacoes() {
        return observacoes != null ? observacoes : motivoConsulta;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
        if (this.motivoConsulta == null) {
            this.motivoConsulta = observacoes;
        }
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}