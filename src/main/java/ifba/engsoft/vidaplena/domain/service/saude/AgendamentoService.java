package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import ifba.engsoft.vidaplena.domain.repository.saude.AgendamentoRepository;
import ifba.engsoft.vidaplena.domain.service.saude.exception.ConflitoHorarioException;
import ifba.engsoft.vidaplena.domain.service.saude.exception.RegraNegocioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public Agendamento criarAgendamento(Agendamento agendamento) {
        // Validação 1: A dataHoraInicio deve ser sempre uma data/hora futura em relação ao momento atual
        validarDataFutura(agendamento.getDataHoraInicio());

        // Validação 2: A dataHoraFim deve ser obrigatoriamente posterior à dataHoraInicio
        validarDataFimPosteriorInicio(agendamento.getDataHoraInicio(), agendamento.getDataHoraFim());

        // Validação 3: Verificar choque de horário (overlap)
        verificarChoqueHorario(agendamento);

        // Salvar o agendamento
        Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);

        // Disparar evento de criação
        eventPublisher.publishEvent(new AgendamentoCriadoEvent(this, agendamentoSalvo));

        return agendamentoSalvo;
    }

    @Transactional
    public Agendamento atualizarStatus(UUID id, StatusAgendamento novoStatus) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Agendamento não encontrado"));

        // Verificar se o status está sendo alterado para CANCELADO
        if (novoStatus == StatusAgendamento.CANCELADO) {
            // Se estiver cancelando, verificar se já está cancelado
            if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
                throw new RegraNegocioException("Agendamento já está cancelado");
            }
        }

        // Armazenar status anterior para o evento
        StatusAgendamento statusAnterior = agendamento.getStatus();
        
        // Atualizar o status
        agendamento.setStatus(novoStatus);
        
        // Salvar o agendamento atualizado
        Agendamento agendamentoAtualizado = agendamentoRepository.save(agendamento);

        // Disparar evento de alteração de status
        eventPublisher.publishEvent(new AgendamentoStatusAlteradoEvent(this, agendamentoAtualizado, statusAnterior));

        return agendamentoAtualizado;
    }

    private void validarDataFutura(LocalDateTime dataHoraInicio) {
        if (dataHoraInicio.isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("A data/hora de início do agendamento deve ser futura");
        }
    }

    private void validarDataFimPosteriorInicio(LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim) {
        if (dataHoraFim.isBefore(dataHoraInicio)) {
            throw new RegraNegocioException("A data/hora de fim do agendamento deve ser posterior à data/hora de início");
        }
    }

    private void verificarChoqueHorario(Agendamento agendamento) {
        // Verificar se há choque de horário para o profissional
        List<Agendamento> agendamentosConflitantes = agendamentoRepository.verificarChoqueHorario(
                agendamento.getProfissional().getId(),
                agendamento.getDataHoraFim(),
                agendamento.getDataHoraInicio(),
                StatusAgendamento.CANCELADO
        );

        if (!agendamentosConflitantes.isEmpty()) {
            throw new ConflitoHorarioException("Já existe um agendamento para o profissional no horário solicitado");
        }
    }
}