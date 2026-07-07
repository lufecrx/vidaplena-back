package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import org.springframework.context.ApplicationEvent;

public class AgendamentoStatusAlteradoEvent extends ApplicationEvent {
    private final Agendamento agendamento;
    private final StatusAgendamento statusAnterior;

    public AgendamentoStatusAlteradoEvent(Object source, Agendamento agendamento, StatusAgendamento statusAnterior) {
        super(source);
        this.agendamento = agendamento;
        this.statusAnterior = statusAnterior;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public StatusAgendamento getStatusAnterior() {
        return statusAnterior;
    }
}