package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.PacienteDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.PacienteResponseDTO;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.model.saude.TipoSanguineo;
import ifba.engsoft.vidaplena.domain.repository.saude.PacienteRepository;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.service.familia.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private Usuario usuario;
    private Paciente paciente;
    private PacienteDTO pacienteDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UUID usuarioId = UUID.randomUUID();
        usuario = new Usuario(
                "João da Silva",
                "12345678910",
                "joao@example.com",
                "senha",
                "71999999999",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)
        );
        ReflectionTestUtils.setField(usuario, "id", usuarioId);

        paciente = new Paciente(usuario);
        paciente.setId(UUID.randomUUID());
        paciente.setTipoSanguineo(TipoSanguineo.O_POSITIVO);
        paciente.setAlergias(List.of("Poeira"));
        paciente.setMedicamentosContinuos(List.of("Nenhum"));
        paciente.setHistoricoFamiliar("Diabetes");

        pacienteDTO = new PacienteDTO(
                usuarioId.toString(),
                TipoSanguineo.O_POSITIVO,
                List.of("Poeira"),
                List.of("Nenhum"),
                "Diabetes"
        );
    }

    @Test
    void deveCriarPacienteComSucesso() {
        when(usuarioRepository.findById(UUID.fromString(pacienteDTO.usuarioId())))
                .thenReturn(Optional.of(usuario));
        when(pacienteRepository.existsByUsuario(usuario)).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        PacienteResponseDTO response = pacienteService.criarPaciente(pacienteDTO);

        assertNotNull(response);
        assertEquals(paciente.getId(), response.id());
        assertEquals(pacienteDTO.usuarioId(), response.usuarioId());
        verify(pacienteRepository, times(1)).save(any(Paciente.class));
    }

    @Test
    void deveLancarExceptionAoCriarPacienteComUsuarioInexistente() {
        when(usuarioRepository.findById(UUID.fromString(pacienteDTO.usuarioId())))
                .thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> pacienteService.criarPaciente(pacienteDTO));
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void deveLancarExceptionAoCriarPacienteComUsuarioJaAssociado() {
        when(usuarioRepository.findById(UUID.fromString(pacienteDTO.usuarioId())))
                .thenReturn(Optional.of(usuario));
        when(pacienteRepository.existsByUsuario(usuario)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> pacienteService.criarPaciente(pacienteDTO));
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void deveAtualizarPacienteComSucesso() {
        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(UUID.fromString(pacienteDTO.usuarioId())))
                .thenReturn(Optional.of(usuario));
        when(pacienteRepository.findByUsuario(usuario)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        PacienteResponseDTO response = pacienteService.atualizarPaciente(paciente.getId(), pacienteDTO);

        assertNotNull(response);
        assertEquals(paciente.getId(), response.id());
        verify(pacienteRepository, times(1)).save(paciente);
    }

    @Test
    void deveLancarExceptionAoAtualizarPacienteInexistente() {
        UUID idInvalido = UUID.randomUUID();
        when(pacienteRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> pacienteService.atualizarPaciente(idInvalido, pacienteDTO));
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void deveLancarExceptionAoAtualizarPacienteComUsuarioAssociadoAOtroPaciente() {
        Paciente outroPaciente = new Paciente(usuario);
        outroPaciente.setId(UUID.randomUUID());

        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(UUID.fromString(pacienteDTO.usuarioId())))
                .thenReturn(Optional.of(usuario));
        when(pacienteRepository.findByUsuario(usuario)).thenReturn(Optional.of(outroPaciente));

        assertThrows(RegraNegocioException.class, () -> pacienteService.atualizarPaciente(paciente.getId(), pacienteDTO));
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void deveObterPacienteComSucesso() {
        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));

        PacienteResponseDTO response = pacienteService.obterPaciente(paciente.getId());

        assertNotNull(response);
        assertEquals(paciente.getId(), response.id());
    }

    @Test
    void deveObterPacientePorUsuarioId() {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(pacienteRepository.findByUsuario(usuario)).thenReturn(Optional.of(paciente));

        PacienteResponseDTO response = pacienteService.obterPorUsuarioId(usuario.getId());

        assertNotNull(response);
        assertEquals(paciente.getId(), response.id());
        assertEquals(usuario.getId().toString(), response.usuarioId());
    }

    @Test
    void deveLancarExceptionAoObterPacienteInexistente() {
        UUID idInvalido = UUID.randomUUID();
        when(pacienteRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> pacienteService.obterPaciente(idInvalido));
    }

    @Test
    void deveListarPacientes() {
        when(pacienteRepository.findAll()).thenReturn(List.of(paciente));

        List<PacienteResponseDTO> list = pacienteService.listarPacientes();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(paciente.getId(), list.get(0).id());
    }

    @Test
    void deveDeletarPacienteComSucesso() {
        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));

        pacienteService.deletarPaciente(paciente.getId());

        verify(pacienteRepository, times(1)).delete(paciente);
    }

    @Test
    void deveLancarExceptionAoDeletarPacienteInexistente() {
        UUID idInvalido = UUID.randomUUID();
        when(pacienteRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> pacienteService.deletarPaciente(idInvalido));
        verify(pacienteRepository, never()).delete(any(Paciente.class));
    }
}
