package ifba.engsoft.vidaplena.interfaces.controller;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.CadastroUsuarioRequest;
import ifba.engsoft.vidaplena.infrastructure.security.TestJwtTokenProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração de segurança para o UsuarioController.
 * Valida que as anotações @PreAuthorize estão corretamente protegendo os endpoints
 * e que o RBAC (Role-Based Access Control) está funcionando com o Spring Security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestJwtTokenProvider testJwtTokenProvider;

    /**
     * ObjectMapper configurado com JavaTimeModule para suportar java.time.LocalDate.
     */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .findAndRegisterModules();

    private static final String ADMIN_EMAIL = "admin.seguranca@example.com";
    private static final String ADMIN_PASSWORD = "Admin@Seguranca123";

    private UUID adminId;

    /**
     * Limpa o repositório e cria um usuário administrador para os testes.
     */
    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        Usuario admin = new Usuario(
                "Administrador de Segurança",
                "00000000000",
                ADMIN_EMAIL,
                passwordEncoder.encode(ADMIN_PASSWORD),
                "71900000000",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.ADMINISTRADOR));

        adminId = usuarioRepository.save(admin).getId();
    }

    /**
     * Cenário de Sucesso: Administrador legítimo consegue buscar dados de um usuário.
     * 
     * Valida que @WithMockUser(roles = "ADMINISTRADOR") + @PreAuthorize("hasRole('ADMINISTRADOR')")
     * permite o acesso ao endpoint protegido.
     */
    @Test
    @DisplayName("Deve permitir que ADMINISTRADOR busque um usuário por ID (HTTP 200)")
    void devePermitirAdministradorBuscarUsuario() throws Exception {
        // Cria um paciente para ser buscado pelo administrador
        Usuario paciente = usuarioRepository.save(new Usuario(
                "Paciente Teste",
                "11111111111",
                "paciente.teste@example.com",
                passwordEncoder.encode("Paciente@123"),
                "71911111111",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)));

        // Gera token JWT válido com role ADMINISTRADOR
        String adminToken = testJwtTokenProvider.generateAdminTestToken(ADMIN_EMAIL);

        mockMvc.perform(get("/api/v1/usuarios/" + paciente.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("paciente.teste@example.com"))
                .andExpect(jsonPath("$.nome").value("Paciente Teste"));
    }

    /**
     * Cenário de Falha: Usuário com role PACIENTE tenta acessar endpoint administrativo.
     * 
     * Valida que @WithMockUser(roles = "PACIENTE") + @PreAuthorize("hasRole('ADMINISTRADOR')")
     * resulta em HTTP 403 Forbidden, impedindo a invasão (privilege escalation).
     */
    @Test
    @DisplayName("Deve negar acesso a PACIENTE tentando buscar usuário por ID (HTTP 403)")
    void deveNegarAcessoPacienteBuscarUsuario() throws Exception {
        // Cria outro usuário para tentar acessar indevidamente
        Usuario alvo = usuarioRepository.save(new Usuario(
                "Alvo do Teste",
                "22222222222",
                "alvo.teste@example.com",
                passwordEncoder.encode("Alvo@123"),
                "71922222222",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)));

        // Gera token JWT válido com role PACIENTE
        String pacienteToken = testJwtTokenProvider.generatePacienteTestToken("paciente.teste@example.com");

        mockMvc.perform(get("/api/v1/usuarios/" + alvo.getId())
                        .header("Authorization", "Bearer " + pacienteToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * Cenário de Sucesso: Administrador legítimo bloqueia um usuário.
     * 
     * Valida que o administrador pode executar operações administrativas sensíveis.
     */
    @Test
    @DisplayName("Deve permitir que ADMINISTRADOR bloqueie um usuário (HTTP 200)")
    void devePermitirAdministradorBloquearUsuario() throws Exception {
        // Cria um usuário que será bloqueado
        Usuario usuarioParaBloquear = usuarioRepository.save(new Usuario(
                "Usuario a Bloquear",
                "33333333333",
                "bloquear.teste@example.com",
                passwordEncoder.encode("Bloquear@123"),
                "71933333333",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)));

        // Gera token JWT válido com role ADMINISTRADOR
        String adminToken = testJwtTokenProvider.generateAdminTestToken(ADMIN_EMAIL);

        String requestJson = objectMapper.writeValueAsString(
                new AlterarStatusUsuarioRequest(StatusUsuario.BLOQUEADO));

        mockMvc.perform(patch("/api/v1/usuarios/" + usuarioParaBloquear.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOQUEADO"));

        // Valida que o status foi persistido no banco H2
        Usuario usuarioAtualizado = usuarioRepository.findById(usuarioParaBloquear.getId()).orElseThrow();
        assertThat(usuarioAtualizado.getStatus()).isEqualTo(StatusUsuario.BLOQUEADO);
    }

    /**
     * Cenário de Falha: Usuário comum tenta bloquear outro usuário.
     * 
     * Valida que um PACIENTE recebe HTTP 403 ao tentar executar operação administrativa.
     */
    @Test
    @DisplayName("Deve negar a PACIENTE tentar bloquear um usuário (HTTP 403)")
    void deveNegarAcessoPacienteBloquearUsuario() throws Exception {
        // Cria um usuário alvo do ataque simulado
        Usuario alvo = usuarioRepository.save(new Usuario(
                "Alvo Bloqueio",
                "44444444444",
                "alvo.bloqueio@example.com",
                passwordEncoder.encode("AlvoBloq@123"),
                "71944444444",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)));

        // Gera token JWT válido com role PACIENTE
        String pacienteToken = testJwtTokenProvider.generatePacienteTestToken("paciente.bloqueio@example.com");

        String requestJson = objectMapper.writeValueAsString(
                new AlterarStatusUsuarioRequest(StatusUsuario.BLOQUEADO));

        mockMvc.perform(patch("/api/v1/usuarios/" + alvo.getId() + "/status")
                        .header("Authorization", "Bearer " + pacienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());

        // Valida que o status NÃO foi alterado (integridade dos dados preservada)
        Usuario alvoAtualizado = usuarioRepository.findById(alvo.getId()).orElseThrow();
        assertThat(alvoAtualizado.getStatus()).isEqualTo(StatusUsuario.ATIVO);
    }

    /**
     * Cenário de Sucesso: Administrador altera tipos de um usuário.
     */
    @Test
    @DisplayName("Deve permitir que ADMINISTRADOR altere tipos de um usuário (HTTP 200)")
    @WithMockUser(roles = "ADMINISTRADOR")
    void devePermitirAdministradorAlterarTiposUsuario() throws Exception {
        Usuario alvo = usuarioRepository.save(new Usuario(
                "Alvo Tipos",
                "55555555555",
                "alvo.tipos@example.com",
                passwordEncoder.encode("AlvoTipos@123"),
                "71955555555",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)));

        String requestJson = objectMapper.writeValueAsString(
                new AlterarTiposUsuarioRequest(Set.of("PACIENTE", "RESPONSAVEL")));

        mockMvc.perform(patch("/api/v1/usuarios/" + alvo.getId() + "/tipos")
                        .header("Authorization", "Bearer fake-jwt-token-for-testing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alvo.tipos@example.com"));
    }

    /**
     * Cenário de Falha: PACIENTE tenta alterar tipos de outro usuário.
     */
    @Test
    @DisplayName("Deve negar a PACIENTE tentar alterar tipos de um usuário (HTTP 403)")
    @WithMockUser(roles = "PACIENTE")
    void deveNegarAcessoPacienteAlterarTiposUsuario() throws Exception {
        Usuario alvo = usuarioRepository.save(new Usuario(
                "Alvo Tipos Negado",
                "66666666666",
                "alvo.tipos.negado@example.com",
                passwordEncoder.encode("AlvoTiposNeg@123"),
                "71966666666",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)));

        String requestJson = objectMapper.writeValueAsString(
                new AlterarTiposUsuarioRequest(Set.of("PACIENTE", "MEDICO")));

        mockMvc.perform(patch("/api/v1/usuarios/" + alvo.getId() + "/tipos")
                        .header("Authorization", "Bearer fake-jwt-token-for-testing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    /**
     * Cenário de Sucesso: Administrador cadastra um novo usuário.
     */
    @Test
    @DisplayName("Deve permitir que ADMINISTRADOR cadastre um novo usuário (HTTP 201)")
    @WithMockUser(roles = "ADMINISTRADOR")
    void devePermitirAdministradorCadastrarUsuario() throws Exception {
        String requestJson = objectMapper.writeValueAsString(new CadastroUsuarioRequest(
                "Novo Paciente",
                "77777777777",
                "novo.paciente@example.com",
                "NovaSenha@123",
                "71977777777",
                LocalDate.of(1990, 5, 15)));

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer fake-jwt-token-for-testing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("novo.paciente@example.com"))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.tipos[0]").value("PACIENTE"));
    }

    @Test
    @DisplayName("Deve permitir que ADMINISTRADOR cadastre um usuário com papéis específicos em rota única atômica (HTTP 201)")
    @WithMockUser(roles = "ADMINISTRADOR")
    void devePermitirAdministradorCadastrarUsuarioComPapeisEspecificos() throws Exception {
        String requestJson = objectMapper.writeValueAsString(new CadastroUsuarioRequest(
                "Dr. Carlos Medico",
                "88888888888",
                "dr.carlos@example.com",
                "Medico@123",
                "71988888888",
                LocalDate.of(1985, 3, 10),
                Set.of(TipoUsuario.MEDICO, TipoUsuario.PACIENTE)));

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer fake-jwt-token-for-testing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("dr.carlos@example.com"))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.tipos", org.hamcrest.Matchers.containsInAnyOrder("MEDICO", "PACIENTE")));
    }

    /**
     * Cenário de Falha: PACIENTE tenta cadastrar um novo usuário (injeção de conta).
     */
    @Test
    @DisplayName("Deve negar a PACIENTE tentar cadastrar um usuário (HTTP 403)")
    @WithMockUser(roles = "PACIENTE")
    void deveNegarAcessoPacienteCadastrarUsuario() throws Exception {
        String requestJson = objectMapper.writeValueAsString(new CadastroUsuarioRequest(
                "Invasor",
                "88888888888",
                "invasor@example.com",
                "Invasor@123",
                "71988888888",
                LocalDate.of(1985, 10, 20)));

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer fake-jwt-token-for-testing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());

        // Valida que nenhum usuário invasor foi criado
        assertThat(usuarioRepository.findByEmailIgnoreCase("invasor@example.com")).isEmpty();
    }

    /**
     * Cenário de Sucesso: Usuário autenticado (qualquer role) acessa seu próprio perfil.
     * 
     * Valida que @PreAuthorize("isAuthenticated()") permite acesso a qualquer usuário autenticado.
     */
    @Test
    @DisplayName("Deve permitir que PACIENTE acesse seu próprio perfil via /me (HTTP 200)")
    @WithMockUser(username = "meu.perfil@example.com", roles = "PACIENTE")
    void devePermitirPacienteAcessarProprioPerfil() throws Exception {
        // Cria o usuário cujo perfil será acessado (email deve bater com @WithMockUser)
        usuarioRepository.save(new Usuario(
                "Meu Perfil Teste",
                "99999999999",
                "meu.perfil@example.com",
                passwordEncoder.encode("MinhaSenha@123"),
                "71999999999",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)));

        mockMvc.perform(get("/api/v1/usuarios/me")
                        .header("Authorization", "Bearer fake-jwt-token-for-testing")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("meu.perfil@example.com"));
    }

    /**
     * Cenário de Falha: Requisição sem token JWT deve ser negada.
     * 
     * Valida que endpoints protegidos retornam HTTP 401 quando não há autenticação.
     */
    @Test
    @DisplayName("Deve negar acesso sem token JWT (HTTP 401)")
    void deveNegarAcessoSemTokenJwt() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/" + adminId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Cenário de Sucesso: Administrador lista usuários com paginação.
     */
    @Test
    @DisplayName("Deve permitir que ADMINISTRADOR liste usuários com paginação (HTTP 200)")
    void devePermitirAdministradorListarUsuariosPaginado() throws Exception {
        usuarioRepository.save(new Usuario(
                "Ana Costa",
                "11122233344",
                "ana.costa@example.com",
                passwordEncoder.encode("Senha@123"),
                "71988880001",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)));

        usuarioRepository.save(new Usuario(
                "Bruno Lima",
                "22233344455",
                "bruno.lima@example.com",
                passwordEncoder.encode("Senha@123"),
                "71988880002",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.MEDICO)));

        String adminToken = testJwtTokenProvider.generateAdminTestToken(ADMIN_EMAIL);

        mockMvc.perform(get("/api/v1/usuarios?page=0&size=10&sort=nome,asc")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.content[0].nome").value("Administrador de Segurança"))
                .andExpect(jsonPath("$.content[1].nome").value("Ana Costa"))
                .andExpect(jsonPath("$.content[2].nome").value("Bruno Lima"));
    }

    /**
     * Cenário de Falha: Paciente tenta listar usuários.
     */
    @Test
    @DisplayName("Deve negar a PACIENTE tentar listar usuários (HTTP 403)")
    void deveNegarAcessoPacienteListarUsuarios() throws Exception {
        String pacienteToken = testJwtTokenProvider.generatePacienteTestToken("paciente.listagem@example.com");

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + pacienteToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * Cenário de Falha: Não autenticado tenta listar usuários.
     */
    @Test
    @DisplayName("Deve negar acesso sem token JWT ao listar usuários (HTTP 401)")
    void deveNegarAcessoSemTokenJwtListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Cenário de Falha: Parâmetro de ordenação inválido (campo inexistente na entidade).
     */
    @Test
    @DisplayName("Deve retornar HTTP 400 Bad Request ao listar com campo de ordenação inexistente")
    void deveRetornarBadRequestAoListarComCampoOrdenacaoInvalido() throws Exception {
        String adminToken = testJwtTokenProvider.generateAdminTestToken(ADMIN_EMAIL);

        mockMvc.perform(get("/api/v1/usuarios?sort=campoInexistente,asc")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetro ou campo de ordenação inválido"));
    }

    /**
     * Cenário de Sucesso: Administrador busca usuário por e-mail.
     */
    @Test
    @DisplayName("Deve permitir que ADMINISTRADOR busque usuário por e-mail (HTTP 200)")
    void devePermitirAdministradorBuscarUsuarioPorEmail() throws Exception {
        usuarioRepository.save(new Usuario(
                "Maria Buscada",
                "12312312312",
                "maria.buscada@example.com",
                passwordEncoder.encode("Senha@123"),
                "71988887777",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)));

        String adminToken = testJwtTokenProvider.generateAdminTestToken(ADMIN_EMAIL);

        mockMvc.perform(get("/api/v1/usuarios/email/maria.buscada@example.com")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Buscada"))
                .andExpect(jsonPath("$.email").value("maria.buscada@example.com"))
                .andExpect(jsonPath("$.cpf").value("12312312312"));
    }

    /**
     * Cenário de Falha: Paciente tenta buscar usuário por e-mail.
     */
    @Test
    @DisplayName("Deve negar a PACIENTE tentar buscar usuário por e-mail (HTTP 403)")
    void deveNegarAcessoPacienteBuscarUsuarioPorEmail() throws Exception {
        String pacienteToken = testJwtTokenProvider.generatePacienteTestToken("paciente.busca@example.com");

        mockMvc.perform(get("/api/v1/usuarios/email/admin.seguranca@example.com")
                        .header("Authorization", "Bearer " + pacienteToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * Cenário de Falha: Busca por e-mail inexistente retorna 404.
     */
    @Test
    @DisplayName("Deve retornar HTTP 404 ao buscar por e-mail inexistente")
    void deveRetornar404AoBuscarEmailInexistente() throws Exception {
        String adminToken = testJwtTokenProvider.generateAdminTestToken(ADMIN_EMAIL);

        mockMvc.perform(get("/api/v1/usuarios/email/naoexiste@example.com")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Cenário de Sucesso: Administrador busca usuário por CPF formatado ou sem pontuação.
     */
    @Test
    @DisplayName("Deve permitir que ADMINISTRADOR busque usuário por CPF (HTTP 200)")
    void devePermitirAdministradorBuscarUsuarioPorCpf() throws Exception {
        usuarioRepository.save(new Usuario(
                "Joao CPF",
                "98765432100",
                "joao.cpf@example.com",
                passwordEncoder.encode("Senha@123"),
                "71977776666",
                null,
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.MEDICO)));

        String adminToken = testJwtTokenProvider.generateAdminTestToken(ADMIN_EMAIL);

        // Busca com formatação
        mockMvc.perform(get("/api/v1/usuarios/cpf/987.654.321-00")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joao CPF"))
                .andExpect(jsonPath("$.email").value("joao.cpf@example.com"));

        // Busca sem pontuação
        mockMvc.perform(get("/api/v1/usuarios/cpf/98765432100")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joao CPF"));
    }

    /**
     * Cenário de Falha: Paciente tenta buscar usuário por CPF.
     */
    @Test
    @DisplayName("Deve negar a PACIENTE tentar buscar usuário por CPF (HTTP 403)")
    void deveNegarAcessoPacienteBuscarUsuarioPorCpf() throws Exception {
        String pacienteToken = testJwtTokenProvider.generatePacienteTestToken("paciente.busca@example.com");

        mockMvc.perform(get("/api/v1/usuarios/cpf/00000000000")
                        .header("Authorization", "Bearer " + pacienteToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * Cenário de Falha: Busca por CPF inexistente retorna 404.
     */
    @Test
    @DisplayName("Deve retornar HTTP 404 ao buscar por CPF inexistente")
    void deveRetornar404AoBuscarCpfInexistente() throws Exception {
        String adminToken = testJwtTokenProvider.generateAdminTestToken(ADMIN_EMAIL);

        mockMvc.perform(get("/api/v1/usuarios/cpf/999.999.999-99")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private record AlterarStatusUsuarioRequest(StatusUsuario status) {
    }

    private record AlterarTiposUsuarioRequest(Set<String> tipos) {
    }
}
