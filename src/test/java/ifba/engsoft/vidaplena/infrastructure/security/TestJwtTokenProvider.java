package ifba.engsoft.vidaplena.infrastructure.security;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.Usuario;

/**
 * Utilitário de teste para gerar tokens JWT válidos com roles específicas.
 * Usado nos testes de segurança para simular autenticação real via JWT.
 */
@Component
public class TestJwtTokenProvider {

    private final JwtService jwtService;

    @Autowired
    public TestJwtTokenProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Gera um token JWT válido para um usuário existente no banco de dados.
     */
    public String generateTestToken(Usuario usuario) {
        return jwtService.generateToken(usuario);
    }

    /**
     * Gera um token JWT válido com roles específicas (sem precisar de um usuário no banco).
     * Cria um objeto Usuario temporário apenas para gerar o token.
     */
    public String generateTestTokenWithRoles(String email, Set<TipoUsuario> tipos) {
        Usuario fakeUser = new Usuario(
                "Test User",
                "00000000000",
                email,
                "password",
                null,
                null,
                StatusUsuario.ATIVO,
                tipos);
        return jwtService.generateToken(fakeUser);
    }

    /**
     * Gera um token JWT válido para um administrador.
     */
    public String generateAdminTestToken(String email) {
        return generateTestTokenWithRoles(email, Set.of(TipoUsuario.ADMINISTRADOR));
    }

    /**
     * Gera um token JWT válido para um paciente.
     */
    public String generatePacienteTestToken(String email) {
        return generateTestTokenWithRoles(email, Set.of(TipoUsuario.PACIENTE));
    }
}
