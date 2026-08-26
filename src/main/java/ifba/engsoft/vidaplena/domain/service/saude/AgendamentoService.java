package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.CriarAgendamentoRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.AgendamentoResponseDTO;
import ifba.engsoft.vidaplena.domain.model.organizacao.Clinica;
import ifba.engsoft.vidaplena.domain.model.organizacao.Organizacao;
import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import ifba.engsoft.vidaplena.domain.repository.organizacao.OrganizacaoRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.AgendamentoRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.PacienteRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.ProfissionalRepository;
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
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public AgendamentoResponseDTO criarAgendamento(CriarAgendamentoRequestDTO dto) {
        // Validação 1: Data e hora futura
        validarDataFutura(dto.dataHora());

        // Validação 2: Existência do paciente
        Paciente paciente = pacienteRepository.findById(dto.pacienteId())
                .orElseThrow(() -> new RegraNegocioException("Paciente não encontrado"));

        // Validação 3: Existência do profissional
        Profissional profissional = profissionalRepository.findById(dto.profissionalId())
                .orElseThrow(() -> new RegraNegocioException("Profissional não encontrado"));

        // Validação 4: Existência da clínica
        Organizacao org = organizacaoRepository.findById(dto.clinicaId())
                .orElseThrow(() -> new RegraNegocioException("Clínica não encontrada"));
        if (!(org instanceof Clinica clinica)) {
            throw new RegraNegocioException("Organização informada não é uma clínica");
        }

        // Validação 5: Choque de horário do profissional (ignorando status CANCELADO)
        boolean conflito = agendamentoRepository.existsByProfissionalIdAndDataHoraAndStatusNot(
                dto.profissionalId(),
                dto.dataHora(),
                StatusAgendamento.CANCELADO
        );
        if (conflito) {
            throw new ConflitoHorarioException("Já existe um agendamento para o profissional no horário solicitado");
        }

        // Construir e salvar entidade
        Agendamento agendamento = new Agendamento();
        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        agendamento.setClinica(clinica);
        agendamento.setPlanoCorporativoId(dto.planoCorporativoId());
        agendamento.setDataHora(dto.dataHora());
        agendamento.setDataHoraInicio(dto.dataHora());
        agendamento.setDataHoraFim(dto.dataHora().plusHours(1));
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setTipoAtendimento(dto.tipoAtendimento());
        agendamento.setObservacoes(dto.observacoes());
        agendamento.setMotivoConsulta(dto.observacoes());

        Agendamento salvo = agendamentoRepository.save(agendamento);

        // Publicar evento
        eventPublisher.publishEvent(new AgendamentoCriadoEvent(this, salvo));

        return mapearParaResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public AgendamentoResponseDTO obterPorId(UUID id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Agendamento não encontrado"));
        return mapearParaResponseDTO(agendamento);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarPorProfissional(UUID profissionalId) {
        if (!profissionalRepository.existsById(profissionalId)) {
            throw new RegraNegocioException("Profissional não encontrado");
        }

        return agendamentoRepository.findByProfissionalIdOrderByDataHoraAsc(profissionalId)
                .stream()
                .map(this::mapearParaResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarPorPaciente(UUID pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new RegraNegocioException("Paciente não encontrado");
        }

        return agendamentoRepository.findByPacienteIdOrderByDataHoraAsc(pacienteId)
                .stream()
                .map(this::mapearParaResponseDTO)
                .toList();
    }

    @Transactional
    public Agendamento atualizarStatus(UUID id, StatusAgendamento novoStatus) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Agendamento não encontrado"));

        if (novoStatus == StatusAgendamento.CANCELADO && agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new RegraNegocioException("Agendamento já está cancelado");
        }

        StatusAgendamento statusAnterior = agendamento.getStatus();
        agendamento.setStatus(novoStatus);
        Agendamento atualizado = agendamentoRepository.save(agendamento);

        eventPublisher.publishEvent(new AgendamentoStatusAlteradoEvent(this, atualizado, statusAnterior));

        return atualizado;
    }

    public AgendamentoResponseDTO mapearParaResponseDTO(Agendamento agendamento) {
        UUID pacienteId = agendamento.getPaciente() != null ? agendamento.getPaciente().getId() : null;
        String pacienteNome = (agendamento.getPaciente() != null && agendamento.getPaciente().getUsuario() != null)
                ? agendamento.getPaciente().getUsuario().getNome()
                : null;

        UUID profissionalId = agendamento.getProfissional() != null ? agendamento.getProfissional().getId() : null;
        String profissionalNome = (agendamento.getProfissional() != null && agendamento.getProfissional().getUsuario() != null)
                ? agendamento.getProfissional().getUsuario().getNome()
                : null;

        UUID clinicaId = agendamento.getClinica() != null
                ? agendamento.getClinica().getId()
                : (agendamento.getProfissional() != null ? agendamento.getProfissional().getClinicaId() : null);

        String clinicaNome = agendamento.getClinica() != null
                ? agendamento.getClinica().getNome()
                : null;

        LocalDateTime dataHora = agendamento.getDataHora() != null
                ? agendamento.getDataHora()
                : agendamento.getDataHoraInicio();

        String observacoes = agendamento.getObservacoes() != null
                ? agendamento.getObservacoes()
                : agendamento.getMotivoConsulta();

        return new AgendamentoResponseDTO(
                agendamento.getId(),
                pacienteId,
                pacienteNome,
                profissionalId,
                profissionalNome,
                clinicaId,
                clinicaNome,
                dataHora,
                agendamento.getStatus(),
                agendamento.getTipoAtendimento(),
                observacoes
        );
    }

    private void validarDataFutura(LocalDateTime dataHora) {
        if (dataHora == null || dataHora.isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("A data/hora do agendamento deve ser futura");
        }
    }
}