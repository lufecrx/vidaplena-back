package ifba.engsoft.vidaplena.domain.model.saude;

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

    @Column(name = "data_hora_inicio", nullable = false)
    @NotNull
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    @NotNull
    private LocalDateTime dataHoraFim;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusAgendamento status;

    @Column(name = "motivo_consulta")
    private String motivoConsulta;

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
        this.status = status;
        this.motivoConsulta = motivoConsulta;
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

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
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

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }
}