package ifba.engsoft.vidaplena.domain.dto.saude;

import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import java.time.LocalDateTime;
import java.util.UUID;

public class AgendamentoResponseDTO {

    private UUID id;
    private String nomePaciente;
    private String nomeProfissional;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private StatusAgendamento status;
    private String motivoConsulta;

    // Construtores
    public AgendamentoResponseDTO() {}

    public AgendamentoResponseDTO(UUID id, String nomePaciente, String nomeProfissional,
                                  LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim,
                                  StatusAgendamento status, String motivoConsulta) {
        this.id = id;
        this.nomePaciente = nomePaciente;
        this.nomeProfissional = nomeProfissional;
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

    public String getNomePaciente() {
        return nomePaciente;
    }

    public void setNomePaciente(String nomePaciente) {
        this.nomePaciente = nomePaciente;
    }

    public String getNomeProfissional() {
        return nomeProfissional;
    }

    public void setNomeProfissional(String nomeProfissional) {
        this.nomeProfissional = nomeProfissional;
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