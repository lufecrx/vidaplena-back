package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.CriarAgendamentoRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.AgendamentoResponseDTO;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.organizacao.Clinica;
import ifba.engsoft.vidaplena.domain.model.organizacao.Empresa;
import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import ifba.engsoft.vidaplena.domain.model.saude.TipoAtendimento;
import ifba.engsoft.vidaplena.domain.repository.organizacao.OrganizacaoRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.AgendamentoRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.PacienteRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.ProfissionalRepository;
import ifba.engsoft.vidaplena.domain.service.saude.exception.ConflitoHorarioException;
import ifba.engsoft.vidaplena.domain.service.saude.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private OrganizacaoRepository organizacaoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private UUID pacienteId;
    private UUID profissionalId;
    private UUID clinicaId;
    private LocalDateTime dataHora;
    private Paciente paciente;
    private Profissional profissional;
    private Clinica clinica;
    private CriarAgendamentoRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        pacienteId = UUID.randomUUID();
        profissionalId = UUID.randomUUID();
        clinicaId = UUID.randomUUID();
        dataHora = LocalDateTime.now().plusDays(1);

        Usuario usuarioPaciente = new Usuario("Paciente Teste", "11122233344", "paciente@test.com", "123", "7199999999", null, null, null);
        paciente = new Paciente(usuarioPaciente);
        paciente.setId(pacienteId);

        Usuario usuarioProfissional = new Usuario("Doutor Teste", "55566677788", "medico@test.com", "123", "7188888888", null, null, null);
        profissional = new Profissional(usuarioProfissional);
        profissional.setId(profissionalId);
        profissional.setClinicaId(clinicaId);

        clinica = new Clinica("Clínica VidaPlena", "12345678000199", "Geral");
        clinica.setId(clinicaId);

        requestDTO = new CriarAgendamentoRequestDTO(
                pacienteId,
                profissionalId,
                clinicaId,
                null,
                dataHora,
                TipoAtendimento.PRESENCIAL,
                "Consulta de Rotina"
        );
    }

    @Test
    @DisplayName("Deve criar agendamento com sucesso")
    void deveCriarAgendamentoComSucesso() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(profissionalId)).thenReturn(Optional.of(profissional));
        when(organizacaoRepository.findById(clinicaId)).thenReturn(Optional.of(clinica));
        when(agendamentoRepository.existsByProfissionalIdAndDataHoraAndStatusNot(
                profissionalId, dataHora, StatusAgendamento.CANCELADO)).thenReturn(false);

        Agendamento agendamentoSalvo = new Agendamento(
                paciente, profissional, clinica, null, dataHora, StatusAgendamento.AGENDADO,
                TipoAtendimento.PRESENCIAL, "Consulta de Rotina"
        );
        agendamentoSalvo.setId(UUID.randomUUID());

        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamentoSalvo);

        AgendamentoResponseDTO response = agendamentoService.criarAgendamento(requestDTO);

        assertNotNull(response);
        assertEquals(agendamentoSalvo.getId(), response.id());
        assertEquals(pacienteId, response.pacienteId());
        assertEquals("Paciente Teste", response.pacienteNome());
        assertEquals(profissionalId, response.profissionalId());
        assertEquals("Doutor Teste", response.profissionalNome());
        assertEquals(clinicaId, response.clinicaId());
        assertEquals("Clínica VidaPlena", response.clinicaNome());
        assertEquals(StatusAgendamento.AGENDADO, response.status());
        assertEquals(TipoAtendimento.PRESENCIAL, response.tipoAtendimento());
        assertEquals("Consulta de Rotina", response.observacoes());

        verify(agendamentoRepository, times(1)).save(any(Agendamento.class));
        verify(eventPublisher, times(1)).publishEvent(any(AgendamentoCriadoEvent.class));
    }

    @Test
    @DisplayName("Deve lançar ConflitoHorarioException quando o profissional já possuir agendamento ativo no horário")
    void deveLancarExceptionAoCriarAgendamentoComChoqueDeHorario() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(profissionalId)).thenReturn(Optional.of(profissional));
        when(organizacaoRepository.findById(clinicaId)).thenReturn(Optional.of(clinica));
        when(agendamentoRepository.existsByProfissionalIdAndDataHoraAndStatusNot(
                profissionalId, dataHora, StatusAgendamento.CANCELADO)).thenReturn(true);

        assertThrows(ConflitoHorarioException.class, () -> agendamentoService.criarAgendamento(requestDTO));

        verify(agendamentoRepository, never()).save(any(Agendamento.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Deve lançar RegraNegocioException ao criar agendamento com data/hora no passado")
    void deveLancarExceptionAoCriarAgendamentoComDataPassada() {
        CriarAgendamentoRequestDTO dtoPassado = new CriarAgendamentoRequestDTO(
                pacienteId,
                profissionalId,
                clinicaId,
                null,
                LocalDateTime.now().minusHours(1),
                TipoAtendimento.PRESENCIAL,
                "Consulta no passado"
        );

        assertThrows(RegraNegocioException.class, () -> agendamentoService.criarAgendamento(dtoPassado));
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Deve lançar RegraNegocioException quando o paciente não for encontrado")
    void deveLancarExceptionQuandoPacienteNaoEncontrado() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> agendamentoService.criarAgendamento(requestDTO));
        assertTrue(ex.getMessage().contains("Paciente não encontrado"));
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve lançar RegraNegocioException quando o profissional não for encontrado")
    void deveLancarExceptionQuandoProfissionalNaoEncontrado() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(profissionalId)).thenReturn(Optional.empty());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> agendamentoService.criarAgendamento(requestDTO));
        assertTrue(ex.getMessage().contains("Profissional não encontrado"));
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve lançar RegraNegocioException quando a clínica não for encontrada")
    void deveLancarExceptionQuandoClinicaNaoEncontrada() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(profissionalId)).thenReturn(Optional.of(profissional));
        when(organizacaoRepository.findById(clinicaId)).thenReturn(Optional.empty());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> agendamentoService.criarAgendamento(requestDTO));
        assertTrue(ex.getMessage().contains("Clínica não encontrada"));
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve lançar RegraNegocioException quando a organização encontrada não for clínica")
    void deveLancarExceptionQuandoOrganizacaoNaoForClinica() {
        Empresa empresa = new Empresa("Empresa Teste", "99888777000166", "TI");
        empresa.setId(clinicaId);

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(profissionalId)).thenReturn(Optional.of(profissional));
        when(organizacaoRepository.findById(clinicaId)).thenReturn(Optional.of(empresa));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> agendamentoService.criarAgendamento(requestDTO));
        assertTrue(ex.getMessage().contains("Organização informada não é uma clínica"));
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve listar agendamentos por profissional ordenados por dataHora asc")
    void deveListarAgendamentosPorProfissionalComSucesso() {
        when(profissionalRepository.existsById(profissionalId)).thenReturn(true);

        Agendamento agendamento1 = new Agendamento(
                paciente, profissional, clinica, null, dataHora, StatusAgendamento.AGENDADO,
                TipoAtendimento.PRESENCIAL, "Consulta 1"
        );
        agendamento1.setId(UUID.randomUUID());

        Agendamento agendamento2 = new Agendamento(
                paciente, profissional, clinica, null, dataHora.plusHours(2), StatusAgendamento.CONFIRMADO,
                TipoAtendimento.TELECONSULTA, "Consulta 2"
        );
        agendamento2.setId(UUID.randomUUID());

        when(agendamentoRepository.findByProfissionalIdOrderByDataHoraAsc(profissionalId))
                .thenReturn(List.of(agendamento1, agendamento2));

        List<AgendamentoResponseDTO> lista = agendamentoService.listarPorProfissional(profissionalId);

        assertNotNull(lista);
        assertEquals(2, lista.size());
        assertEquals("Consulta 1", lista.get(0).observacoes());
        assertEquals("Consulta 2", lista.get(1).observacoes());
        assertEquals(TipoAtendimento.PRESENCIAL, lista.get(0).tipoAtendimento());
        assertEquals(TipoAtendimento.TELECONSULTA, lista.get(1).tipoAtendimento());
    }

    @Test
    @DisplayName("Deve lançar RegraNegocioException ao listar por profissional inexistente")
    void deveLancarExceptionAoListarPorProfissionalInexistente() {
        when(profissionalRepository.existsById(profissionalId)).thenReturn(false);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> agendamentoService.listarPorProfissional(profissionalId));
        assertTrue(ex.getMessage().contains("Profissional não encontrado"));
        verify(agendamentoRepository, never()).findByProfissionalIdOrderByDataHoraAsc(any());
    }

    @Test
    @DisplayName("Deve atualizar status do agendamento com sucesso")
    void deveAtualizarStatusComSucesso() {
        UUID agendamentoId = UUID.randomUUID();
        Agendamento agendamento = new Agendamento(
                paciente, profissional, clinica, null, dataHora, StatusAgendamento.AGENDADO,
                TipoAtendimento.PRESENCIAL, "Consulta"
        );
        agendamento.setId(agendamentoId);

        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(i -> i.getArgument(0));

        Agendamento atualizado = agendamentoService.atualizarStatus(agendamentoId, StatusAgendamento.CONFIRMADO);

        assertNotNull(atualizado);
        assertEquals(StatusAgendamento.CONFIRMADO, atualizado.getStatus());
        verify(eventPublisher, times(1)).publishEvent(any(AgendamentoStatusAlteradoEvent.class));
    }

    @Test
    @DisplayName("Deve lançar RegraNegocioException ao atualizar status de agendamento inexistente")
    void deveLancarExceptionAoAtualizarStatusDeAgendamentoInexistente() {
        UUID idInvalido = UUID.randomUUID();
        when(agendamentoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> agendamentoService.atualizarStatus(idInvalido, StatusAgendamento.CONFIRMADO));
    }

    @Test
    @DisplayName("Deve lançar RegraNegocioException ao cancelar agendamento já cancelado")
    void deveLancarExceptionAoCancelarAgendamentoJaCancelado() {
        UUID agendamentoId = UUID.randomUUID();
        Agendamento agendamento = new Agendamento(
                paciente, profissional, clinica, null, dataHora, StatusAgendamento.CANCELADO,
                TipoAtendimento.PRESENCIAL, "Consulta"
        );
        agendamento.setId(agendamentoId);

        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));

        assertThrows(RegraNegocioException.class, () -> agendamentoService.atualizarStatus(agendamentoId, StatusAgendamento.CANCELADO));
    }
}
