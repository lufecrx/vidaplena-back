package ifba.engsoft.vidaplena.interfaces.controller;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FUNCIONARIO_ADMINISTRATIVO')")
	public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CadastroUsuarioRequest request) {
		UsuarioResponse response = usuarioService.criarUsuario(request, Set.of(TipoUsuario.PACIENTE), StatusUsuario.ATIVO);
		return ResponseEntity.status(201).body(response);
	}

	@PatchMapping("/{usuarioId}/status")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FUNCIONARIO_ADMINISTRATIVO')")
	public ResponseEntity<UsuarioResponse> alterarStatus(@PathVariable UUID usuarioId,
			@Valid @RequestBody AlterarStatusUsuarioRequest request) {
		return ResponseEntity.ok(usuarioService.atualizarStatus(usuarioId, request.status()));
	}

	@PatchMapping("/{usuarioId}/tipos")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FUNCIONARIO_ADMINISTRATIVO')")
	public ResponseEntity<UsuarioResponse> alterarTipos(@PathVariable UUID usuarioId,
			@Valid @RequestBody AlterarTiposUsuarioRequest request) {
		return ResponseEntity.ok(usuarioService.atualizarTipos(usuarioId, request.tipos()));
	}

	@GetMapping("/{usuarioId}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FUNCIONARIO_ADMINISTRATIVO')")
	public ResponseEntity<UsuarioResponse> buscar(@PathVariable UUID usuarioId) {
		return ResponseEntity.ok(usuarioService.toResponse(usuarioService.buscarUsuario(usuarioId)));
	}
}