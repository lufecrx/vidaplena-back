package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.model.saude.*;
import ifba.engsoft.vidaplena.domain.repository.saude.*;
import ifba.engsoft.vidaplena.domain.service.saude.exception.ProntuarioNotFoundException;
import ifba.engsoft.vidaplena.domain.service.saude.exception.RegraNegocioException;
import ifba.engsoft.vidaplena.domain.service.saude.exception.RegistroImutavelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProntuarioService {

    @Autowired
    private ProntuarioRepository prontuarioRepository;

    @Autowired
    private RegistroAtendimentoRepository registroAtendimentoRepository;

    @Autowired
    private NotaRetificacaoRepository notaRetificacaoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Transactional
    public Prontuario criarProntuario(Prontuario prontuario) {
        // Verificar se o paciente existe
        Paciente paciente = pacienteRepository.findById(prontuario.getPaciente().getId())
                .orElseThrow(() -> new RegraNegocioException("Paciente não encontrado"));
        prontuario.setPaciente(paciente);

        // Verificar se já existe um prontuário para este paciente
        Optional<Prontuario> prontuarioExistente = Optional.ofNullable(prontuarioRepository.findByPacienteId(paciente.getId()));
        if (prontuarioExistente.isPresent()) {
            throw new RegraNegocioException("Já existe um prontuário cadastrado para este paciente");
        }

        return prontuarioRepository.save(prontuario);
    }

    @Transactional
    public RegistroAtendimento criarRegistroAtendimento(RegistroAtendimento registroAtendimento) {
        // Verificar se o prontuário existe
        Prontuario prontuario = prontuarioRepository.findById(registroAtendimento.getProntuario().getId())
                .orElseThrow(() -> new ProntuarioNotFoundException("Prontuário não encontrado"));
        registroAtendimento.setProntuario(prontuario);

        // Verificar se o profissional existe
        Profissional profissional = profissionalRepository.findById(registroAtendimento.getProfissional().getId())
                .orElseThrow(() -> new RegraNegocioException("Profissional não encontrado"));
        registroAtendimento.setProfissional(profissional);

        // Verificar se o agendamento existe
        Agendamento agendamento = agendamentoRepository.findById(registroAtendimento.getAgendamento().getId())
                .orElseThrow(() -> new RegraNegocioException("Agendamento não encontrado"));
        
        // Validar se o agendamento está concluído
        if (agendamento.getStatus() != StatusAgendamento.CONCLUIDO) {
            throw new RegraNegocioException("O registro de atendimento só pode ser criado para um agendamento concluído.");
        }

        // Verificar se o agendamento pertence ao paciente do prontuário
        if (!agendamento.getPaciente().getId().equals(prontuario.getPaciente().getId())) {
            throw new RegraNegocioException("O agendamento fornecido não pertence ao paciente deste prontuário.");
        }

        // Verificar se já existe um registro de atendimento para este agendamento (evitar duplicatas)
        if (registroAtendimentoRepository.existsByAgendamentoId(agendamento.getId())) {
            throw new RegraNegocioException("Já existe um registro de atendimento vinculado a este agendamento.");
        }

        registroAtendimento.setAgendamento(agendamento);
        registroAtendimento.setDataRegistro(LocalDateTime.now());

        return registroAtendimentoRepository.save(registroAtendimento);
    }

    @Transactional
    public RegistroAtendimento atualizarRegistroAtendimento(UUID id, RegistroAtendimento dadosAtualizados) {
        RegistroAtendimento registroExistente = registroAtendimentoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Registro de atendimento não encontrado"));

        // Se já estiver finalizado, impede qualquer alteração
        if (registroExistente.isFinalizado()) {
            throw new RegistroImutavelException("Este registro de atendimento foi finalizado e não pode ser editado. Correções devem ser feitas via Nota de Retificação.");
        }

        // Atualiza campos permitidos (evolução clínica)
        registroExistente.setSintomasRelatados(dadosAtualizados.getSintomasRelatados());
        registroExistente.setDiagnostico(dadosAtualizados.getDiagnostico());
        registroExistente.setPrescricaoMedica(dadosAtualizados.getPrescricaoMedica());
        registroExistente.setPrescricaoEnfermagem(dadosAtualizados.getPrescricaoEnfermagem());
        registroExistente.setNotasClinicas(dadosAtualizados.getNotasClinicas());
        registroExistente.setFinalizado(dadosAtualizados.isFinalizado());

        return registroAtendimentoRepository.save(registroExistente);
    }

    @Transactional
    public void deletarRegistroAtendimento(UUID id) {
        RegistroAtendimento registroExistente = registroAtendimentoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Registro de atendimento não encontrado"));

        // Se já estiver finalizado, impede a exclusão
        if (registroExistente.isFinalizado()) {
            throw new RegistroImutavelException("Este registro de atendimento foi finalizado e não pode ser excluído.");
        }

        registroAtendimentoRepository.delete(registroExistente);
    }

    @Transactional
    public NotaRetificacao adicionarNotaRetificacao(UUID registroAtendimentoId, UUID profissionalId, String texto) {
        RegistroAtendimento registroExistente = registroAtendimentoRepository.findById(registroAtendimentoId)
                .orElseThrow(() -> new RegraNegocioException("Registro de atendimento não encontrado"));

        // Nota de retificação só pode ser adicionada a atendimentos finalizados
        if (!registroExistente.isFinalizado()) {
            throw new RegraNegocioException("Notas de retificação só podem ser adicionadas a registros de atendimento finalizados. Para registros em rascunho, edite o registro diretamente.");
        }

        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RegraNegocioException("Profissional não encontrado"));

        NotaRetificacao nota = new NotaRetificacao(registroExistente, profissional, texto);
        return notaRetificacaoRepository.save(nota);
    }

    public Prontuario obterProntuarioPorId(UUID id) {
        return prontuarioRepository.findById(id)
                .orElseThrow(() -> new ProntuarioNotFoundException("Prontuário não encontrado"));
    }

    public Prontuario obterProntuarioPorPacienteId(UUID pacienteId) {
        // Verificar se paciente existe
        pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RegraNegocioException("Paciente não encontrado"));

        Prontuario prontuario = prontuarioRepository.findByPacienteId(pacienteId);
        if (prontuario == null) {
            throw new ProntuarioNotFoundException("Prontuário não cadastrado para o paciente informado.");
        }
        return prontuario;
    }

    public List<RegistroAtendimento> obterRegistrosAtendimentoPorProntuario(UUID prontuarioId) {
        // Verifica se prontuário existe
        if (!prontuarioRepository.existsById(prontuarioId)) {
            throw new ProntuarioNotFoundException("Prontuário não encontrado");
        }
        return registroAtendimentoRepository.findAllByProntuarioIdOrderByDataRegistroAsc(prontuarioId);
    }

    public RegistroAtendimento obterRegistroAtendimentoPorId(UUID id) {
        return registroAtendimentoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Registro de atendimento não encontrado"));
    }

    public List<RegistroAtendimento> obterHistoricoClinicoPaciente(UUID pacienteId) {
        Prontuario prontuario = obterProntuarioPorPacienteId(pacienteId);
        return registroAtendimentoRepository.findAllByProntuarioIdOrderByDataRegistroAsc(prontuario.getId());
    }
}