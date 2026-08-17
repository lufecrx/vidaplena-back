package ifba.engsoft.vidaplena.domain.service;

import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.UsuarioResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuarioId = UUID.randomUUID();
        usuario = new Usuario(
                "Carlos Silva",
                "12345678901",
                "carlos.silva@example.com",
                "encodedPassword",
                "71999998888",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)
        );
        ReflectionTestUtils.setField(usuario, "id", usuarioId);
    }

    @Test
    @DisplayName("Deve listar usuários com paginação com sucesso")
    void deveListarUsuariosComPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> paginaUsuarios = new PageImpl<>(List.of(usuario), pageable, 1);

        when(usuarioRepository.findAll(pageable)).thenReturn(paginaUsuarios);

        Page<UsuarioResponse> resultado = usuarioService.listarUsuarios(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).id()).isEqualTo(usuarioId);
        assertThat(resultado.getContent().get(0).nome()).isEqualTo("Carlos Silva");
        assertThat(resultado.getContent().get(0).email()).isEqualTo("carlos.silva@example.com");

        verify(usuarioRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void deveBuscarUsuarioPorIdComSucesso() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarUsuario(usuarioId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(usuarioId);
        assertThat(resultado.getNome()).isEqualTo("Carlos Silva");

        verify(usuarioRepository, times(1)).findById(usuarioId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar usuário por ID inexistente")
    void deveLancarExcecaoAoBuscarUsuarioInexistente() {
        UUID inexistenteId = UUID.randomUUID();
        when(usuarioRepository.findById(inexistenteId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> usuarioService.buscarUsuario(inexistenteId));

        verify(usuarioRepository, times(1)).findById(inexistenteId);
    }

    @Test
    @DisplayName("Deve atualizar status do usuário com sucesso")
    void deveAtualizarStatusUsuarioComSucesso() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        UsuarioResponse response = usuarioService.atualizarStatus(usuarioId, StatusUsuario.INATIVO);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(StatusUsuario.INATIVO);
        assertThat(usuario.getStatus()).isEqualTo(StatusUsuario.INATIVO);
    }

    @Test
    @DisplayName("Deve atualizar tipos do usuário com sucesso")
    void deveAtualizarTiposUsuarioComSucesso() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        UsuarioResponse response = usuarioService.atualizarTipos(usuarioId, Set.of(TipoUsuario.MEDICO, TipoUsuario.PACIENTE));

        assertThat(response).isNotNull();
        assertThat(response.tipos()).containsExactlyInAnyOrder(TipoUsuario.MEDICO, TipoUsuario.PACIENTE);
    }
}
