package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;

public interface NotificacaoService {
    void notificarAgendamentoCriado(Agendamento agendamento);
    void notificarAlteracaoStatus(Agendamento agendamento);
}