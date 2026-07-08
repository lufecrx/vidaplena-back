package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.model.saude.*;
import ifba.engsoft.vidaplena.domain.repository.saude.*;
import ifba.engsoft.vidaplena.domain.service.saude.exception.RegraNegocioException;
import ifba.engsoft.vidaplena.domain.service.saude.exception.RegistroImutavelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProntuarioServiceTest {

    @Mock
    private ProntuarioRepository prontuarioRepository;

    @Mock
    private RegistroAtendimentoRepository registroAtendimentoRepository;

    @Mock
    private NotaRetificacaoRepository notaRetificacaoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private ProntuarioService prontuarioService;

    private Paciente paciente;
    private Profissional profissional;
    private Agendamento agendamento;
    private Prontuario prontuario;
    private RegistroAtendimento registro;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        UUID pacienteId = UUID.randomUUID();
        paciente = new Paciente();
        paciente.setId(pacienteId);

        UUID profissionalId = UUID.randomUUID();
        profissional = new Profissional();
        profissional.setId(profissionalId);

        UUID agendamentoId = UUID.randomUUID();
        agendamento = new Agendamento();
        agendamento.setId(agendamentoId);
        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        agendamento.setStatus(StatusAgendamento.CONCLUIDO);

        prontuario = new Prontuario(paciente);
        prontuario.setId(UUID.randomUUID());

        registro = new RegistroAtendimento(prontuario, profissional, agendamento);
        registro.setId(UUID.randomUUID());
    }

    @Test
    public void testCriarProntuarioComSucesso() {
        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
        when(prontuarioRepository.findByPacienteId(paciente.getId())).thenReturn(null);
        when(prontuarioRepository.save(any(Prontuario.class))).thenReturn(prontuario);

        Prontuario result = prontuarioService.criarProntuario(prontuario);

        assertNotNull(result);
        assertEquals(prontuario.getId(), result.getId());
        verify(prontuarioRepository, times(1)).save(prontuario);
    }

    @Test
    public void testCriarProntuarioComPacienteJaPossuiProntuario() {
        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
        when(prontuarioRepository.findByPacienteId(paciente.getId())).thenReturn(prontuario);

        assertThrows(RegraNegocioException.class, () -> {
            prontuarioService.criarProntuario(prontuario);
        });
    }

    @Test
    public void testCriarRegistroAtendimentoComSucesso() {
        when(prontuarioRepository.findById(registro.getProntuario().getId())).thenReturn(Optional.of(prontuario));
        when(profissionalRepository.findById(registro.getProfissional().getId())).thenReturn(Optional.of(profissional));
        when(agendamentoRepository.findById(registro.getAgendamento().getId())).thenReturn(Optional.of(agendamento));
        when(registroAtendimentoRepository.existsByAgendamentoId(agendamento.getId())).thenReturn(false);
        when(registroAtendimentoRepository.save(any(RegistroAtendimento.class))).thenReturn(registro);

        RegistroAtendimento result = prontuarioService.criarRegistroAtendimento(registro);

        assertNotNull(result);
        assertEquals(registro.getId(), result.getId());
        verify(registroAtendimentoRepository, times(1)).save(registro);
    }

    @Test
    public void testCriarRegistroAtendimentoComAgendamentoNaoConcluido() {
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        when(prontuarioRepository.findById(registro.getProntuario().getId())).thenReturn(Optional.of(prontuario));
        when(profissionalRepository.findById(registro.getProfissional().getId())).thenReturn(Optional.of(profissional));
        when(agendamentoRepository.findById(registro.getAgendamento().getId())).thenReturn(Optional.of(agendamento));

        assertThrows(RegraNegocioException.class, () -> {
            prontuarioService.criarRegistroAtendimento(registro);
        });
    }

    @Test
    public void testAtualizarRegistroAtendimentoRascunhoComSucesso() {
        registro.setFinalizado(false);
        RegistroAtendimento dadosNovos = new RegistroAtendimento();
        dadosNovos.setSintomasRelatados("Novos sintomas");
        dadosNovos.setFinalizado(true);

        when(registroAtendimentoRepository.findById(registro.getId())).thenReturn(Optional.of(registro));
        when(registroAtendimentoRepository.save(any(RegistroAtendimento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroAtendimento result = prontuarioService.atualizarRegistroAtendimento(registro.getId(), dadosNovos);

        assertNotNull(result);
        assertTrue(result.isFinalizado());
        assertEquals("Novos sintomas", result.getSintomasRelatados());
    }

    @Test
    public void testAtualizarRegistroAtendimentoFinalizadoThrowsException() {
        registro.setFinalizado(true);
        RegistroAtendimento dadosNovos = new RegistroAtendimento();

        when(registroAtendimentoRepository.findById(registro.getId())).thenReturn(Optional.of(registro));

        assertThrows(RegistroImutavelException.class, () -> {
            prontuarioService.atualizarRegistroAtendimento(registro.getId(), dadosNovos);
        });
    }

    @Test
    public void testDeletarRegistroAtendimentoRascunhoComSucesso() {
        registro.setFinalizado(false);
        when(registroAtendimentoRepository.findById(registro.getId())).thenReturn(Optional.of(registro));

        prontuarioService.deletarRegistroAtendimento(registro.getId());

        verify(registroAtendimentoRepository, times(1)).delete(registro);
    }

    @Test
    public void testDeletarRegistroAtendimentoFinalizadoThrowsException() {
        registro.setFinalizado(true);
        when(registroAtendimentoRepository.findById(registro.getId())).thenReturn(Optional.of(registro));

        assertThrows(RegistroImutavelException.class, () -> {
            prontuarioService.deletarRegistroAtendimento(registro.getId());
        });
    }

    @Test
    public void testAdicionarNotaRetificacaoComSucesso() {
        registro.setFinalizado(true);
        NotaRetificacao nota = new NotaRetificacao(registro, profissional, "Retificação");
        
        when(registroAtendimentoRepository.findById(registro.getId())).thenReturn(Optional.of(registro));
        when(profissionalRepository.findById(profissional.getId())).thenReturn(Optional.of(profissional));
        when(notaRetificacaoRepository.save(any(NotaRetificacao.class))).thenReturn(nota);

        NotaRetificacao result = prontuarioService.adicionarNotaRetificacao(registro.getId(), profissional.getId(), "Retificação");

        assertNotNull(result);
        assertEquals("Retificação", result.getTexto());
        verify(notaRetificacaoRepository, times(1)).save(any(NotaRetificacao.class));
    }

    @Test
    public void testAdicionarNotaRetificacaoEmRascunhoThrowsException() {
        registro.setFinalizado(false);
        when(registroAtendimentoRepository.findById(registro.getId())).thenReturn(Optional.of(registro));

        assertThrows(RegraNegocioException.class, () -> {
            prontuarioService.adicionarNotaRetificacao(registro.getId(), profissional.getId(), "Retificação");
        });
    }
}