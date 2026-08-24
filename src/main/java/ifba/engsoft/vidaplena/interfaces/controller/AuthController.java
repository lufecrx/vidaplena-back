package ifba.engsoft.vidaplena.interfaces.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.service.AuthService;
import ifba.engsoft.vidaplena.domain.service.UsuarioService;
import ifba.engsoft.vidaplena.infrastructure.exception.ApiErrorResponse;
import ifba.engsoft.vidaplena.interfaces.dto.auth.AuthResponse;
import ifba.engsoft.vidaplena.interfaces.dto.auth.ForgotPasswordRequest;
import ifba.engsoft.vidaplena.interfaces.dto.auth.LoginRequest;
import ifba.engsoft.vidaplena.interfaces.dto.auth.MessageResponse;
import ifba.engsoft.vidaplena.interfaces.dto.auth.ResetPasswordRequest;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.CadastroUsuarioRequest;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.UsuarioResponse;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints públicos para login, auto-cadastro de pacientes e fluxo de recuperação de senha")
public class AuthController {

	private final AuthService authService;
	private final UsuarioService usuarioService;

	public AuthController(AuthService authService, UsuarioService usuarioService) {
		this.authService = authService;
		this.usuarioService = usuarioService;
	}

	@Operation(
			summary = "Cadastrar novo usuário (Auto-cadastro público)",
			description = "Cria um novo usuário na plataforma com os papéis/tipos informados (padrão PACIENTE caso não informado) e status PENDENTE_VALIDACAO.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "201",
					description = "Usuário cadastrado com sucesso",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
			@ApiResponse(
					responseCode = "400",
					description = "Dados inválidos de cadastro (CPF, e-mail já existente ou senha fraca)",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Erro interno no servidor ao processar o cadastro",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
	})
	@PostMapping("/register")
	public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody CadastroUsuarioRequest request) {
		Set<TipoUsuario> tipos = (request.tipos() != null && !request.tipos().isEmpty())
				? request.tipos()
				: Set.of(TipoUsuario.PACIENTE);
		UsuarioResponse response = usuarioService.criarUsuario(request, tipos, StatusUsuario.PENDENTE_VALIDACAO);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(
			summary = "Autenticar usuário (Login)",
			description = "Valida as credenciais de e-mail e senha do usuário, gerando o token de acesso Bearer JWT para as demais operações autenticadas.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200",
					description = "Autenticação realizada com sucesso e token JWT retornado",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
			@ApiResponse(
					responseCode = "400",
					description = "Corpo da requisição inválido ou campos obrigatórios ausentes",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "401",
					description = "Credenciais inválidas, usuário não encontrado ou conta bloqueada/inativa",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Erro interno no servidor durante a autenticação",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
	})
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.autenticar(request));
	}

	@Operation(
			summary = "Solicitar recuperação de senha (Esqueci a senha)",
			description = "Gera um token de recuperação de senha seguro e envia por e-mail, caso a conta exista.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "202",
					description = "Solicitação recebida e processada de forma assíncrona/segura",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
			@ApiResponse(
					responseCode = "400",
					description = "Formato de e-mail inválido",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Erro interno no servidor ao disparar e-mail de recuperação",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
	})
	@PostMapping("/forgot-password")
	public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.solicitarRedefinicaoSenha(request);
		return ResponseEntity.accepted().body(new MessageResponse("Se o e-mail existir, o token de redefinição será enviado."));
	}

	@Operation(
			summary = "Redefinir senha com token",
			description = "Redefine a senha de acesso da conta utilizando o token de recuperação enviado previamente ao e-mail do usuário.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200",
					description = "Senha redefinida com sucesso",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
			@ApiResponse(
					responseCode = "400",
					description = "Token expirado, token inválido ou requisitos de complexidade de senha não atendidos",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Erro interno no servidor ao redefinir a senha",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
	})
	@PostMapping("/reset-password")
	public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.redefinirSenha(request);
		return ResponseEntity.ok(new MessageResponse("Senha redefinida com sucesso."));
	}
}