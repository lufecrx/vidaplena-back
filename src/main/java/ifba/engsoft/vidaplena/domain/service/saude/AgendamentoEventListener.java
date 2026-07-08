package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.service.saude.impl.NotificacaoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AgendamentoEventListener {

    @Autowired
    private NotificacaoServiceImpl notificacaoService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAgendamentoCriado(AgendamentoCriadoEvent event) {
        Agendamento agendamento = event.getAgendamento();
        notificacaoService.notificarAgendamentoCriado(agendamento);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAgendamentoStatusAlterado(AgendamentoStatusAlteradoEvent event) {
        Agendamento agendamento = event.getAgendamento();
        notificacaoService.notificarAlteracaoStatus(agendamento);
    }
}