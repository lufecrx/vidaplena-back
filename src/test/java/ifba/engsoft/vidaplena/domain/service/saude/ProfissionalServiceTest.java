package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.ProfissionalDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.ProfissionalResponseDTO;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.saude.Especialidade;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import ifba.engsoft.vidaplena.domain.repository.saude.ProfissionalRepository;
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

class ProfissionalServiceTest {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ProfissionalService profissionalService;

    private Usuario usuario;
    private Profissional profissional;
    private ProfissionalDTO profissionalDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UUID usuarioId = UUID.randomUUID();
        usuario = new Usuario(
                "Dr. Silveira",
                "12345678910",
                "silveira@example.com",
                "senha",
                "71999999999",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.MEDICO)
        );
        ReflectionTestUtils.setField(usuario, "id", usuarioId);

        profissional = new Profissional(usuario);
        profissional.setId(UUID.randomUUID());
        profissional.setRegistroConselho("CRM/BA 12345");
        profissional.setEspecialidade(Especialidade.CLINICO_GERAL);
        profissional.setClinicaId(UUID.randomUUID());

        profissionalDTO = new ProfissionalDTO(
                usuarioId.toString(),
                "CRM/BA 12345",
                Especialidade.CLINICO_GERAL,
                profissional.getClinicaId()
        );
    }

    @Test
    void deveCriarProfissionalComSucesso() {
        when(usuarioRepository.findById(UUID.fromString(profissionalDTO.usuarioId())))
                .thenReturn(Optional.of(usuario));
        when(profissionalRepository.existsByUsuario(usuario)).thenReturn(false);
        when(profissionalRepository.existsByRegistroConselho(profissionalDTO.registroConselho())).thenReturn(false);
        when(profissionalRepository.save(any(Profissional.class))).thenReturn(profissional);

        ProfissionalResponseDTO response = profissionalService.criarProfissional(profissionalDTO);

        assertNotNull(response);
        assertEquals(profissional.getId(), response.id());
        assertEquals(profissionalDTO.usuarioId(), response.usuarioId());
        verify(profissionalRepository, times(1)).save(any(Profissional.class));
    }

    @Test
    void deveLancarExceptionAoCriarProfissionalComUsuarioInexistente() {
        when(usuarioRepository.findById(UUID.fromString(profissionalDTO.usuarioId())))
                .thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> profissionalService.criarProfissional(profissionalDTO));
        verify(profissionalRepository, never()).save(any(Profissional.class));
    }

    @Test
    void deveLancarExceptionAoCriarProfissionalComUsuarioJaAssociado() {
        when(usuarioRepository.findById(UUID.fromString(profissionalDTO.usuarioId())))
                .thenReturn(Optional.of(usuario));
        when(profissionalRepository.existsByUsuario(usuario)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> profissionalService.criarProfissional(profissionalDTO));
        verify(profissionalRepository, never()).save(any(Profissional.class));
    }

    @Test
    void deveLancarExceptionAoCriarProfissionalComRegistroConselhoJaExistente() {
        when(usuarioRepository.findById(UUID.fromString(profissionalDTO.usuarioId())))
                .thenReturn(Optional.of(usuario));
        when(profissionalRepository.existsByUsuario(usuario)).thenReturn(false);
        when(profissionalRepository.existsByRegistroConselho(profissionalDTO.registroConselho())).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> profissionalService.criarProfissional(profissionalDTO));
        verify(profissionalRepository, never()).save(any(Profissional.class));
    }

    @Test
    void deveAtualizarProfissionalComSucesso() {
        when(profissionalRepository.findById(profissional.getId())).thenReturn(Optional.of(profissional));
        when(usuarioRepository.findById(UUID.fromString(profissionalDTO.usuarioId())))
                .thenReturn(Optional.of(usuario));
        when(profissionalRepository.findByUsuario(usuario)).thenReturn(Optional.of(profissional));
        when(profissionalRepository.findByRegistroConselho(profissionalDTO.registroConselho()))
                .thenReturn(Optional.of(profissional));
        when(profissionalRepository.save(any(Profissional.class))).thenReturn(profissional);

        ProfissionalResponseDTO response = profissionalService.atualizarProfissional(profissional.getId(), profissionalDTO);

        assertNotNull(response);
        assertEquals(profissional.getId(), response.id());
        verify(profissionalRepository, times(1)).save(profissional);
    }

    @Test
    void deveLancarExceptionAoAtualizarProfissionalInexistente() {
        UUID idInvalido = UUID.randomUUID();
        when(profissionalRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> profissionalService.atualizarProfissional(idInvalido, profissionalDTO));
        verify(profissionalRepository, never()).save(any(Profissional.class));
    }

    @Test
    void deveLancarExceptionAoAtualizarProfissionalComUsuarioAssociadoAOtro() {
        Profissional outroProfissional = new Profissional(usuario);
        outroProfissional.setId(UUID.randomUUID());

        when(profissionalRepository.findById(profissional.getId())).thenReturn(Optional.of(profissional));
        when(usuarioRepository.findById(UUID.fromString(profissionalDTO.usuarioId())))
                .thenReturn(Optional.of(usuario));
        when(profissionalRepository.findByUsuario(usuario)).thenReturn(Optional.of(outroProfissional));

        assertThrows(RegraNegocioException.class, () -> profissionalService.atualizarProfissional(profissional.getId(), profissionalDTO));
        verify(profissionalRepository, never()).save(any(Profissional.class));
    }

    @Test
    void deveLancarExceptionAoAtualizarProfissionalComRegistroConselhoDeOutro() {
        Profissional outroProfissional = new Profissional(usuario);
        outroProfissional.setId(UUID.randomUUID());

        when(profissionalRepository.findById(profissional.getId())).thenReturn(Optional.of(profissional));
        when(usuarioRepository.findById(UUID.fromString(profissionalDTO.usuarioId())))
                .thenReturn(Optional.of(usuario));
        when(profissionalRepository.findByUsuario(usuario)).thenReturn(Optional.of(profissional));
        when(profissionalRepository.findByRegistroConselho(profissionalDTO.registroConselho()))
                .thenReturn(Optional.of(outroProfissional));

        assertThrows(RegraNegocioException.class, () -> profissionalService.atualizarProfissional(profissional.getId(), profissionalDTO));
        verify(profissionalRepository, never()).save(any(Profissional.class));
    }

    @Test
    void deveObterProfissionalComSucesso() {
        when(profissionalRepository.findById(profissional.getId())).thenReturn(Optional.of(profissional));

        ProfissionalResponseDTO response = profissionalService.obterProfissional(profissional.getId());

        assertNotNull(response);
        assertEquals(profissional.getId(), response.id());
    }

    @Test
    void deveObterProfissionalPorUsuarioId() {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(profissionalRepository.findByUsuario(usuario)).thenReturn(Optional.of(profissional));

        ProfissionalResponseDTO response = profissionalService.obterPorUsuarioId(usuario.getId());

        assertNotNull(response);
        assertEquals(profissional.getId(), response.id());
        assertEquals(usuario.getId().toString(), response.usuarioId());
    }

    @Test
    void deveLancarExceptionAoObterProfissionalInexistente() {
        UUID idInvalido = UUID.randomUUID();
        when(profissionalRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> profissionalService.obterProfissional(idInvalido));
    }

    @Test
    void deveListarProfissionais() {
        when(profissionalRepository.findAll()).thenReturn(List.of(profissional));

        List<ProfissionalResponseDTO> list = profissionalService.listarProfissionais();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(profissional.getId(), list.get(0).id());
    }

    @Test
    void deveDeletarProfissionalComSucesso() {
        when(profissionalRepository.findById(profissional.getId())).thenReturn(Optional.of(profissional));

        profissionalService.deletarProfissional(profissional.getId());

        verify(profissionalRepository, times(1)).delete(profissional);
    }

    @Test
    void deveLancarExceptionAoDeletarProfissionalInexistente() {
        UUID idInvalido = UUID.randomUUID();
        when(profissionalRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> profissionalService.deletarProfissional(idInvalido));
        verify(profissionalRepository, never()).delete(any(Profissional.class));
    }
}
