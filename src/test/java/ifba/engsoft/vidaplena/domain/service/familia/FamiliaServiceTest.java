package ifba.engsoft.vidaplena.domain.service.familia;

import ifba.engsoft.vidaplena.domain.dto.familia.FamiliaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.FamiliaResponseDTO;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.familia.Familia;
import ifba.engsoft.vidaplena.domain.model.familia.VinculoDependencia;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.FamiliaRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.VinculoDependenciaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FamiliaServiceTest {

    @Mock
    private FamiliaRepository familiaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VinculoDependenciaRepository vinculoDependenciaRepository;

    @InjectMocks
    private FamiliaService familiaService;

    private Familia familia;
    private Usuario membro;
    private FamiliaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        membro = new Usuario(
                "Membro da Familia",
                "11122233344",
                "membro@example.com",
                "senha",
                "71999999999",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)
        );
        UUID membroId = UUID.randomUUID();
        ReflectionTestUtils.setField(membro, "id", membroId);

        familia = new Familia("Familia Silva");
        familia.setId(UUID.randomUUID());
        familia.setMembros(new ArrayList<>(List.of(membro)));

        requestDTO = new FamiliaRequestDTO("Familia Silva", List.of(membroId));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveCriarFamiliaComSucesso() {
        when(usuarioRepository.existsById(membro.getId())).thenReturn(true);
        when(usuarioRepository.findAllById(anyList())).thenReturn(List.of(membro));
        when(familiaRepository.save(any(Familia.class))).thenReturn(familia);

        FamiliaResponseDTO response = familiaService.criarFamilia(requestDTO);

        assertNotNull(response);
        assertEquals(familia.getNome(), response.nome());
        verify(familiaRepository, times(1)).save(any(Familia.class));
    }

    @Test
    void deveLancarExceptionAoCriarFamiliaComMembroInexistente() {
        when(usuarioRepository.existsById(membro.getId())).thenReturn(false);

        assertThrows(RegraNegocioException.class, () -> familiaService.criarFamilia(requestDTO));
        verify(familiaRepository, never()).save(any(Familia.class));
    }

    @Test
    void deveExcluirFamiliaComSucesso() {
        when(familiaRepository.findById(familia.getId())).thenReturn(Optional.of(familia));
        when(familiaRepository.countActiveVinculosByFamiliaId(familia.getId())).thenReturn(0L);
        // Limpar membros para permitir exclusão
        familia.setMembros(List.of());

        familiaService.excluirFamilia(familia.getId());

        verify(familiaRepository, times(1)).deleteById(familia.getId());
    }

    @Test
    void deveLancarExceptionAoExcluirFamiliaInexistente() {
        UUID idInvalido = UUID.randomUUID();
        when(familiaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> familiaService.excluirFamilia(idInvalido));
    }

    @Test
    void deveLancarExceptionAoExcluirFamiliaComVinculosAtivos() {
        when(familiaRepository.findById(familia.getId())).thenReturn(Optional.of(familia));
        when(familiaRepository.countActiveVinculosByFamiliaId(familia.getId())).thenReturn(2L);

        assertThrows(RegraNegocioException.class, () -> familiaService.excluirFamilia(familia.getId()));
        verify(familiaRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deveLancarExceptionAoExcluirFamiliaComMembros() {
        when(familiaRepository.findById(familia.getId())).thenReturn(Optional.of(familia));
        when(familiaRepository.countActiveVinculosByFamiliaId(familia.getId())).thenReturn(0L);

        assertThrows(RegraNegocioException.class, () -> familiaService.excluirFamilia(familia.getId()));
        verify(familiaRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deveInativarFamiliaComSucesso() {
        when(familiaRepository.findById(familia.getId())).thenReturn(Optional.of(familia));
        when(vinculoDependenciaRepository.findByResponsavelIdInAndDataFimIsNull(anyList()))
                .thenReturn(List.of(new VinculoDependencia()));
        when(familiaRepository.save(any(Familia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FamiliaResponseDTO response = familiaService.inativarFamilia(familia.getId());

        assertNotNull(response);
        assertTrue(response.nome().startsWith("[INATIVO]"));
        verify(vinculoDependenciaRepository, times(1)).save(any(VinculoDependencia.class));
        verify(familiaRepository, times(1)).save(any(Familia.class));
    }

    @Test
    void deveBuscarFamiliaComSucessoParaAdmin() {
        mockAuthentication("admin@example.com", TipoUsuario.ADMINISTRADOR);
        Usuario admin = new Usuario(
                "Admin",
                "22233344455",
                "admin@example.com",
                "senha",
                "71999999999",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.ADMINISTRADOR)
        );
        when(usuarioRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(familiaRepository.findById(familia.getId())).thenReturn(Optional.of(familia));

        FamiliaResponseDTO response = familiaService.buscarFamilia(familia.getId());

        assertNotNull(response);
        assertEquals(familia.getNome(), response.nome());
    }

    @Test
    void deveBuscarFamiliaComSucessoParaMembro() {
        mockAuthentication("membro@example.com", TipoUsuario.PACIENTE);
        when(usuarioRepository.findByEmailIgnoreCase("membro@example.com")).thenReturn(Optional.of(membro));
        when(familiaRepository.findById(familia.getId())).thenReturn(Optional.of(familia));

        FamiliaResponseDTO response = familiaService.buscarFamilia(familia.getId());

        assertNotNull(response);
        assertEquals(familia.getNome(), response.nome());
    }

    @Test
    void deveLancarExceptionAoBuscarFamiliaParaNaoMembro() {
        mockAuthentication("outro@example.com", TipoUsuario.PACIENTE);
        Usuario outro = new Usuario(
                "Outro",
                "55566677788",
                "outro@example.com",
                "senha",
                "71999999999",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)
        );
        UUID outroId = UUID.randomUUID();
        ReflectionTestUtils.setField(outro, "id", outroId);

        when(usuarioRepository.findByEmailIgnoreCase("outro@example.com")).thenReturn(Optional.of(outro));
        when(familiaRepository.findById(familia.getId())).thenReturn(Optional.of(familia));

        assertThrows(AccessDeniedException.class, () -> familiaService.buscarFamilia(familia.getId()));
    }

    @Test
    void deveListarTodasFamilias() {
        when(familiaRepository.findAll()).thenReturn(List.of(familia));

        List<FamiliaResponseDTO> list = familiaService.buscarTodasFamilias();

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    private void mockAuthentication(String email, TipoUsuario role) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }
}
