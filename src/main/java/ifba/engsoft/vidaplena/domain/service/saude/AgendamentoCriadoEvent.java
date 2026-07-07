package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import org.springframework.context.ApplicationEvent;

public class AgendamentoCriadoEvent extends ApplicationEvent {
    private final Agendamento agendamento;

    public AgendamentoCriadoEvent(Object source, Agendamento agendamento) {
        super(source);
        this.agendamento = agendamento;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }
}