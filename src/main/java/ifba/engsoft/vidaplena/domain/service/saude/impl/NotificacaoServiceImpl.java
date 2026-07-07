package ifba.engsoft.vidaplena.domain.service.saude.impl;

import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.service.saude.NotificacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class NotificacaoServiceImpl implements NotificacaoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoServiceImpl.class);

    @Override
    public void notificarAgendamentoCriado(Agendamento agendamento) {
        Paciente paciente = agendamento.getPaciente();
        Profissional profissional = agendamento.getProfissional();
        
        // Acessando os dados do usuário através da entidade Paciente e Profissional
        Usuario usuarioPaciente = paciente.getUsuario();
        Usuario usuarioProfissional = profissional.getUsuario();
        
        String mensagem = String.format(
            "Novo agendamento criado para %s (%s) - Profissional: %s, Data/Hora: %s a %s, Status: %s",
            usuarioPaciente.getNome(),
            usuarioPaciente.getEmail(),
            usuarioProfissional.getNome(),
            agendamento.getDataHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            agendamento.getDataHoraFim().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            agendamento.getStatus()
        );
        
        logger.info("Notificação de agendamento criado: {}", mensagem);
        // Aqui seria o código para enviar e-mail/SMS real
    }

    @Override
    public void notificarAlteracaoStatus(Agendamento agendamento) {
        Paciente paciente = agendamento.getPaciente();
        Profissional profissional = agendamento.getProfissional();
        
        // Acessando os dados do usuário através da entidade Paciente e Profissional
        Usuario usuarioPaciente = paciente.getUsuario();
        Usuario usuarioProfissional = profissional.getUsuario();
        
        String mensagem = String.format(
            "Status do agendamento alterado para %s - Paciente: %s (%s), Profissional: %s, Data/Hora: %s a %s",
            agendamento.getStatus(),
            usuarioPaciente.getNome(),
            usuarioPaciente.getEmail(),
            usuarioProfissional.getNome(),
            agendamento.getDataHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            agendamento.getDataHoraFim().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        
        logger.info("Notificação de alteração de status: {}", mensagem);
        // Aqui seria o código para enviar e-mail/SMS real
    }
}