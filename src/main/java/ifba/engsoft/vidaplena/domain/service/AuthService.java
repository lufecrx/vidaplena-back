package ifba.engsoft.vidaplena.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ifba.engsoft.vidaplena.domain.model.PasswordResetToken;
import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.repository.PasswordResetTokenRepository;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.infrastructure.mail.MailService;
import ifba.engsoft.vidaplena.infrastructure.security.JwtService;
import ifba.engsoft.vidaplena.interfaces.dto.auth.AuthResponse;
import ifba.engsoft.vidaplena.interfaces.dto.auth.ForgotPasswordRequest;
import ifba.engsoft.vidaplena.interfaces.dto.auth.LoginRequest;
import ifba.engsoft.vidaplena.interfaces.dto.auth.ResetPasswordRequest;

@Service
@Transactional
public class AuthService {

	private static final Duration PASSWORD_RESET_TTL = Duration.ofMinutes(30);

	private final AuthenticationManager authenticationManager;
	private final UsuarioService usuarioService;
	private final UsuarioRepository usuarioRepository;
	private final JwtService jwtService;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final MailService mailService;
	private final SecureRandom secureRandom = new SecureRandom();

	public AuthService(AuthenticationManager authenticationManager, UsuarioService usuarioService, JwtService jwtService,
			PasswordResetTokenRepository passwordResetTokenRepository, UsuarioRepository usuarioRepository, MailService mailService) {
		this.authenticationManager = authenticationManager;
		this.usuarioService = usuarioService;
		this.usuarioRepository = usuarioRepository;
		this.jwtService = jwtService;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.mailService = mailService;
	}

	public AuthResponse autenticar(LoginRequest request) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

		Usuario usuario = usuarioService.buscarPorEmail(request.email());
		if (usuario.getStatus() != StatusUsuario.ATIVO && usuario.getStatus() != StatusUsuario.PENDENTE_VALIDACAO) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta inativa ou bloqueada");
		}

		String token = jwtService.generateToken(usuario);
		return new AuthResponse(token, "Bearer", usuarioService.toResponse(usuario));
	}

	public void solicitarRedefinicaoSenha(ForgotPasswordRequest request) {
		usuarioRepository.findByEmailIgnoreCase(normalizarEmail(request.email()))
				.ifPresent(this::gerarTokenRedefinicao);
	}

	public void redefinirSenha(ResetPasswordRequest request) {
		String tokenHash = hashToken(request.token());
		PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHashAndUsedAtIsNull(tokenHash)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou expirado"));

		if (resetToken.isExpired(Instant.now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou expirado");
		}

		usuarioService.atualizarSenha(resetToken.getUsuario().getId(), request.novaSenha());
		resetToken.markAsUsed(Instant.now());
	}

	public void gerarTokenRedefinicao(Usuario usuario) {
		String rawToken = gerarTokenSeguro();
		String tokenHash = hashToken(rawToken);
		Instant agora = Instant.now();
		PasswordResetToken resetToken = new PasswordResetToken(usuario, tokenHash, agora, agora.plus(PASSWORD_RESET_TTL));
		passwordResetTokenRepository.save(resetToken);
		mailService.sendPasswordResetToken(usuario, rawToken);
	}

	private String gerarTokenSeguro() {
		byte[] token = new byte[32];
		secureRandom.nextBytes(token);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
	}

	private String hashToken(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 indisponível", ex);
		}
	}

	private String normalizarEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}
}