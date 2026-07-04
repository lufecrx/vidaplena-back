package ifba.engsoft.vidaplena;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ifba.engsoft.vidaplena.domain.model.PasswordResetToken;
import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.repository.PasswordResetTokenRepository;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiFlowTests {

	private static final String ADMIN_EMAIL = "admin@example.com";
	private static final String ADMIN_PASSWORD = "Admin@123";

	@LocalServerPort
	private int port;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private HttpClient httpClient;

	@BeforeEach
	void prepararBase() {
		httpClient = HttpClient.newHttpClient();
		passwordResetTokenRepository.deleteAll();
		usuarioRepository.deleteAll();
		usuarioRepository.save(new Usuario(
				"Administrador",
				"99999999999",
				ADMIN_EMAIL,
				passwordEncoder.encode(ADMIN_PASSWORD),
				"71900000000",
				null,
				StatusUsuario.ATIVO,
				Set.of(TipoUsuario.ADMINISTRADOR)));
	}

	@Test
	void deveCadastrarUsuarioLogarERecuperarSenha() throws Exception {
		HttpResult cadastro = postJson("/api/v1/auth/register", Map.of(
				"nome", "Maria Silva",
				"cpf", "123.456.789-10",
				"email", "maria@example.com",
				"senha", "Senha@123",
				"telefone", "71999999999",
				"dataNascimento", "1990-01-15"), null);

		assertThat(cadastro.statusCode()).isEqualTo(201);
		assertThat(cadastro.body()).containsEntry("email", "maria@example.com");
		assertThat(cadastro.body()).containsEntry("status", "PENDENTE_VALIDACAO");

		HttpResult login = postJson("/api/v1/auth/login", Map.of(
				"email", "maria@example.com",
				"senha", "Senha@123"), null);

		assertThat(login.statusCode()).isEqualTo(200);
		assertThat((String) login.body().get("accessToken")).isNotBlank();

		HttpResult forgot = postJson("/api/v1/auth/forgot-password", Map.of("email", "maria@example.com"), null);
		assertThat(forgot.statusCode()).isEqualTo(202);
		assertThat(passwordResetTokenRepository.findAll()).hasSize(1);
	}

	@Test
	void deveAceitarRecuperacaoParaEmailInexistenteSemVazarInformacao() throws Exception {
		HttpResult response = postJson("/api/v1/auth/forgot-password", Map.of("email", "inexistente@example.com"), null);

		assertThat(response.statusCode()).isEqualTo(202);
		assertThat(passwordResetTokenRepository.findAll()).isEmpty();
	}

	@Test
	void deveRedefinirSenhaComTokenValido() throws Exception {
		Usuario usuario = usuarioRepository.save(new Usuario(
				"Joao Teste",
				"12345678910",
				"joao@example.com",
				passwordEncoder.encode("Senha@123"),
				"71988888888",
				java.time.LocalDate.of(1988, 3, 10),
				StatusUsuario.ATIVO,
				Set.of(TipoUsuario.PACIENTE)));

		String rawToken = "reset-token-123";
		passwordResetTokenRepository.save(new PasswordResetToken(usuario, hash(rawToken), Instant.now(), Instant.now().plusSeconds(1800)));

		HttpResult reset = postJson("/api/v1/auth/reset-password", Map.of(
				"token", rawToken,
				"novaSenha", "NovaSenha@123"), null);

		assertThat(reset.statusCode()).isEqualTo(200);

		HttpResult login = postJson("/api/v1/auth/login", Map.of(
				"email", "joao@example.com",
				"senha", "NovaSenha@123"), null);
		assertThat(login.statusCode()).isEqualTo(200);
		assertThat((String) login.body().get("accessToken")).isNotBlank();
	}

	@Test
	void deveGerenciarUsuarioEPerfisComoAdministrador() throws Exception {
		HttpResult create = postJson("/api/v1/usuarios", Map.of(
				"nome", "Paciente Admin",
				"cpf", "987.654.321-00",
				"email", "paciente.admin@example.com",
				"senha", "Senha@123",
				"telefone", "71977777777",
				"dataNascimento", "1995-05-20"), basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD));

		assertThat(create.statusCode()).isEqualTo(201);

		var usuarioId = usuarioRepository.findByEmailIgnoreCase("paciente.admin@example.com").orElseThrow().getId();

		HttpResult status = patchJson("/api/v1/usuarios/" + usuarioId + "/status", Map.of("status", "BLOQUEADO"), basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD));
		assertThat(status.statusCode()).isEqualTo(200);
		assertThat(status.body()).containsEntry("status", "BLOQUEADO");

		HttpResult tipos = patchJson("/api/v1/usuarios/" + usuarioId + "/tipos", Map.of("tipos", Set.of("PACIENTE", "RESPONSAVEL")), basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD));
		assertThat(tipos.statusCode()).isEqualTo(200);

		HttpResult buscar = getJson("/api/v1/usuarios/" + usuarioId, basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD));
		assertThat(buscar.statusCode()).isEqualTo(200);
		assertThat(buscar.body()).containsEntry("email", "paciente.admin@example.com");
	}

	@Test
	void devePadronizarErrosDeValidacao() throws Exception {
		HttpResult response = postJson("/api/v1/auth/register", Map.of(
				"nome", "",
				"cpf", "",
				"email", "email-invalido",
				"senha", "123"), null);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).containsEntry("message", "Dados inválidos");
		assertThat((java.util.List<?>) response.body().get("details")).isNotEmpty();
	}

	private HttpResult postJson(String path, Map<String, Object> body, String authorization) throws Exception {
		return sendJson("POST", path, body, authorization);
	}

	private HttpResult patchJson(String path, Map<String, Object> body, String authorization) throws Exception {
		return sendJson("PATCH", path, body, authorization);
	}

	private HttpResult getJson(String path, String authorization) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path)).GET();
		applyAuthorization(builder, authorization);
		builder.header("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
		return new HttpResult(response.statusCode(), parseBody(response.body()));
	}

	private HttpResult sendJson(String method, String path, Map<String, Object> body, String authorization) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path))
				.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.header("Accept", MediaType.APPLICATION_JSON_VALUE)
				.method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
		applyAuthorization(builder, authorization);
		HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
		return new HttpResult(response.statusCode(), parseBody(response.body()));
	}

	private void applyAuthorization(HttpRequest.Builder builder, String authorization) {
		if (authorization != null) {
			builder.header("Authorization", authorization);
		}
	}

	private String basicAuth(String username, String password) {
		String token = username + ":" + password;
		return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
	}

	private URI uri(String path) {
		return URI.create("http://localhost:" + port + path);
	}

	private Map<String, Object> parseBody(String body) throws IOException {
		return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
		});
	}

	private String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private record HttpResult(int statusCode, Map<String, Object> body) {
	}
}
