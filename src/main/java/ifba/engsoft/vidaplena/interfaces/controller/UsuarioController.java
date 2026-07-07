package ifba.engsoft.vidaplena.interfaces.controller;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.service.UsuarioService;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.AlterarStatusUsuarioRequest;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.AlterarTiposUsuarioRequest;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.CadastroUsuarioRequest;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.UsuarioResponse;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	/**
	 * Criação de novos administradores ou alteração de status de contas.
	 * Restrito exclusivamente a usuários com ROLE_ADMINISTRADOR.
	 */
	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CadastroUsuarioRequest request) {
		UsuarioResponse response = usuarioService.criarUsuario(request, Set.of(TipoUsuario.PACIENTE), StatusUsuario.ATIVO);
		return ResponseEntity.status(201).body(response);
	}

	/**
	 * Alteração de status de conta (ativar/inativar/bloquear).
	 * Restrito exclusivamente a usuários com ROLE_ADMINISTRADOR.
	 */
	@PatchMapping("/{usuarioId}/status")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<UsuarioResponse> alterarStatus(@PathVariable UUID usuarioId,
			@Valid @RequestBody AlterarStatusUsuarioRequest request) {
		return ResponseEntity.ok(usuarioService.atualizarStatus(usuarioId, request.status()));
	}

	/**
	 * Alteração de tipos de conta.
	 * Restrito exclusivamente a usuários com ROLE_ADMINISTRADOR.
	 */
	@PatchMapping("/{usuarioId}/tipos")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<UsuarioResponse> alterarTipos(@PathVariable UUID usuarioId,
			@Valid @RequestBody AlterarTiposUsuarioRequest request) {
		return ResponseEntity.ok(usuarioService.atualizarTipos(usuarioId, request.tipos()));
	}

	/**
	 * Busca de perfil por ID. (Admin)
	 * 
	 * O @PreAuthorize com hasRole('ADMINISTRADOR') permite que apenas administradores acessem qualquer perfil. 
	 */
	@GetMapping("/{usuarioId}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<UsuarioResponse> buscar(@PathVariable UUID usuarioId, Authentication authentication) {
		UsuarioResponse response = usuarioService.toResponse(usuarioService.buscarUsuario(usuarioId));
		return ResponseEntity.ok(response);
	}

	/**
	 * Busca do próprio perfil (meu perfil).
	 * Permite que o usuário autenticado consulte seus próprios dados.
	 */
	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UsuarioResponse> buscarMeuPerfil(Authentication authentication) {
		String email = authentication.getName();
		UsuarioResponse response = usuarioService.toResponse(usuarioService.buscarPorEmail(email));
		return ResponseEntity.ok(response);
	}

	/**
	 * Atualização dos dados do próprio usuário.
	 * Permite que o usuário atualize apenas seus próprios dados.
	 */
	@PatchMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UsuarioResponse> atualizarMeusDados(Authentication authentication,
			@Valid @RequestBody CadastroUsuarioRequest request) {
		String email = authentication.getName();
		
		if (!email.equals(request.email())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o proprietário pode atualizar seus próprios dados");
		}
		
		UsuarioResponse response = usuarioService.toResponse(usuarioService.buscarPorEmail(email));
		return ResponseEntity.ok(response);
	}
}
