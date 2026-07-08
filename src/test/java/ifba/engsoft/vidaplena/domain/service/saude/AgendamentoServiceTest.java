package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import ifba.engsoft.vidaplena.domain.repository.saude.AgendamentoRepository;
import ifba.engsoft.vidaplena.domain.service.saude.exception.ConflitoHorarioException;
import ifba.engsoft.vidaplena.domain.service.saude.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Agendamento agendamento;
    private Paciente paciente;
    private Profissional profissional;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        paciente = new Paciente();
        paciente.setId(UUID.randomUUID());

        profissional = new Profissional();
        profissional.setId(UUID.randomUUID());

        agendamento = new Agendamento();
        agendamento.setId(UUID.randomUUID());
        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        agendamento.setDataHoraInicio(LocalDateTime.now().plusDays(1));
        agendamento.setDataHoraFim(LocalDateTime.now().plusDays(1).plusHours(1));
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setMotivoConsulta("Rotina");
    }

    @Test
    void deveCriarAgendamentoComSucesso() {
        when(agendamentoRepository.verificarChoqueHorario(
                any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class), eq(StatusAgendamento.CANCELADO)))
                .thenReturn(List.of());
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);

        Agendamento response = agendamentoService.criarAgendamento(agendamento);

        assertNotNull(response);
        assertEquals(agendamento.getId(), response.getId());
        verify(agendamentoRepository, times(1)).save(agendamento);
        verify(eventPublisher, times(1)).publishEvent(any(AgendamentoCriadoEvent.class));
    }

    @Test
    void deveLancarExceptionAoCriarAgendamentoComDataInicioPassada() {
        agendamento.setDataHoraInicio(LocalDateTime.now().minusHours(1));

        assertThrows(RegraNegocioException.class, () -> agendamentoService.criarAgendamento(agendamento));
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deveLancarExceptionAoCriarAgendamentoComDataFimAnteriorAInicio() {
        agendamento.setDataHoraFim(agendamento.getDataHoraInicio().minusMinutes(30));

        assertThrows(RegraNegocioException.class, () -> agendamentoService.criarAgendamento(agendamento));
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deveLancarExceptionAoCriarAgendamentoComChoqueDeHorario() {
        when(agendamentoRepository.verificarChoqueHorario(
                any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class), eq(StatusAgendamento.CANCELADO)))
                .thenReturn(List.of(new Agendamento()));

        assertThrows(ConflitoHorarioException.class, () -> agendamentoService.criarAgendamento(agendamento));
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deveAtualizarStatusComSucesso() {
        when(agendamentoRepository.findById(agendamento.getId())).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);

        Agendamento response = agendamentoService.atualizarStatus(agendamento.getId(), StatusAgendamento.CONFIRMADO);

        assertNotNull(response);
        assertEquals(StatusAgendamento.CONFIRMADO, response.getStatus());
        verify(agendamentoRepository, times(1)).save(agendamento);
        verify(eventPublisher, times(1)).publishEvent(any(AgendamentoStatusAlteradoEvent.class));
    }

    @Test
    void deveLancarExceptionAoAtualizarStatusDeAgendamentoInexistente() {
        UUID idInvalido = UUID.randomUUID();
        when(agendamentoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> agendamentoService.atualizarStatus(idInvalido, StatusAgendamento.CONFIRMADO));
    }

    @Test
    void deveLancarExceptionAoCancelarAgendamentoJaCancelado() {
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        when(agendamentoRepository.findById(agendamento.getId())).thenReturn(Optional.of(agendamento));

        assertThrows(RegraNegocioException.class, () -> agendamentoService.atualizarStatus(agendamento.getId(), StatusAgendamento.CANCELADO));
    }
}
