package ifba.engsoft.vidaplena.domain.dto.saude;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class AgendamentoRequestDTO {

    @NotNull(message = "O ID do paciente é obrigatório")
    private UUID idPaciente;

    @NotNull(message = "O ID do profissional é obrigatório")
    private UUID idProfissional;

    @NotNull(message = "A data/hora de início é obrigatória")
    @Future(message = "A data/hora de início deve ser futura")
    private LocalDateTime dataHoraInicio;

    @NotNull(message = "A data/hora de fim é obrigatória")
    @Future(message = "A data/hora de fim deve ser futura")
    private LocalDateTime dataHoraFim;

    private String motivoConsulta;

    // Construtores
    public AgendamentoRequestDTO() {}

    public AgendamentoRequestDTO(UUID idPaciente, UUID idProfissional, LocalDateTime dataHoraInicio,
                                 LocalDateTime dataHoraFim, String motivoConsulta) {
        this.idPaciente = idPaciente;
        this.idProfissional = idProfissional;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.motivoConsulta = motivoConsulta;
    }

    // Getters e Setters
    public UUID getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(UUID idPaciente) {
        this.idPaciente = idPaciente;
    }

    public UUID getIdProfissional() {
        return idProfissional;
    }

    public void setIdProfissional(UUID idProfissional) {
        this.idProfissional = idProfissional;
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

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }
}