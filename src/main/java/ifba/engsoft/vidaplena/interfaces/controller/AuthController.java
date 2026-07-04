package ifba.engsoft.vidaplena.interfaces.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.service.AuthService;
import ifba.engsoft.vidaplena.domain.service.UsuarioService;
import ifba.engsoft.vidaplena.interfaces.dto.auth.AuthResponse;
import ifba.engsoft.vidaplena.interfaces.dto.auth.ForgotPasswordRequest;
import ifba.engsoft.vidaplena.interfaces.dto.auth.LoginRequest;
import ifba.engsoft.vidaplena.interfaces.dto.auth.MessageResponse;
import ifba.engsoft.vidaplena.interfaces.dto.auth.ResetPasswordRequest;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.CadastroUsuarioRequest;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.UsuarioResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;
	private final UsuarioService usuarioService;

	public AuthController(AuthService authService, UsuarioService usuarioService) {
		this.authService = authService;
		this.usuarioService = usuarioService;
	}

	@PostMapping("/register")
	public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody CadastroUsuarioRequest request) {
		UsuarioResponse response = usuarioService.criarUsuario(request, Set.of(TipoUsuario.PACIENTE), StatusUsuario.PENDENTE_VALIDACAO);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.autenticar(request));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.solicitarRedefinicaoSenha(request);
		return ResponseEntity.accepted().body(new MessageResponse("Se o e-mail existir, o token de redefinição será enviado."));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.redefinirSenha(request);
		return ResponseEntity.ok(new MessageResponse("Senha redefinida com sucesso."));
	}
}