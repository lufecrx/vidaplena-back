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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.service.UsuarioService;
import ifba.engsoft.vidaplena.infrastructure.exception.ApiErrorResponse;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.AlterarStatusUsuarioRequest;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.AlterarTiposUsuarioRequest;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.CadastroUsuarioRequest;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.UsuarioResponse;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários", description = "Gerenciamento de contas de usuários, perfis, atribuição de papéis e controle de status")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@Operation(
			summary = "Criar novo usuário (Administrativo)",
			description = "Cria um novo usuário na plataforma com status ATIVO. Acesso restrito a administradores.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "201",
					description = "Usuário criado com sucesso",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
			@ApiResponse(
					responseCode = "400",
					description = "Dados inválidos de cadastro ou CPF/e-mail duplicado",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "401",
					description = "Não autenticado / Token ausente ou inválido",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "403",
					description = "Acesso negado - Requer ROLE_ADMINISTRADOR",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Erro interno do servidor",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
	})
	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CadastroUsuarioRequest request) {
		UsuarioResponse response = usuarioService.criarUsuario(request, Set.of(TipoUsuario.PACIENTE), StatusUsuario.ATIVO);
		return ResponseEntity.status(201).body(response);
	}

	@Operation(
			summary = "Alterar status da conta",
			description = "Atualiza o status operacional do usuário (ex: ATIVO, INATIVO, BLOQUEADO). Restrito a administradores.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200",
					description = "Status atualizado com sucesso",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
			@ApiResponse(
					responseCode = "400",
					description = "Status informado inválido",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "401",
					description = "Não autenticado / Token ausente ou inválido",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "403",
					description = "Acesso negado - Requer ROLE_ADMINISTRADOR",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "404",
					description = "Usuário não encontrado para o ID informado",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Erro interno do servidor",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
	})
	@PatchMapping("/{usuarioId}/status")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<UsuarioResponse> alterarStatus(
			@Parameter(description = "Identificador único (UUID) do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
			@PathVariable UUID usuarioId,
			@Valid @RequestBody AlterarStatusUsuarioRequest request) {
		return ResponseEntity.ok(usuarioService.atualizarStatus(usuarioId, request.status()));
	}

	@Operation(
			summary = "Alterar papéis/tipos do usuário",
			description = "Atualiza a lista de perfis/roles atribuídos ao usuário. Restrito a administradores.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200",
					description = "Tipos do usuário atualizados com sucesso",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
			@ApiResponse(
					responseCode = "400",
					description = "Conjunto de tipos vazio ou com valores inválidos",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "401",
					description = "Não autenticado / Token ausente ou inválido",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "403",
					description = "Acesso negado - Requer ROLE_ADMINISTRADOR",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "404",
					description = "Usuário não encontrado para o ID informado",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Erro interno do servidor",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
	})
	@PatchMapping("/{usuarioId}/tipos")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<UsuarioResponse> alterarTipos(
			@Parameter(description = "Identificador único (UUID) do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
			@PathVariable UUID usuarioId,
			@Valid @RequestBody AlterarTiposUsuarioRequest request) {
		return ResponseEntity.ok(usuarioService.atualizarTipos(usuarioId, request.tipos()));
	}

	@Operation(
			summary = "Buscar usuário por ID (Administrativo)",
			description = "Permite a um administrador consultar os dados cadastrais de qualquer conta.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200",
					description = "Usuário localizado com sucesso",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
			@ApiResponse(
					responseCode = "401",
					description = "Não autenticado / Token ausente ou inválido",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "403",
					description = "Acesso negado - Requer ROLE_ADMINISTRADOR",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "404",
					description = "Usuário não encontrado para o ID informado",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Erro interno do servidor",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
	})
	@GetMapping("/{usuarioId}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<UsuarioResponse> buscar(
			@Parameter(description = "Identificador único (UUID) do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
			@PathVariable UUID usuarioId,
			Authentication authentication) {
		UsuarioResponse response = usuarioService.toResponse(usuarioService.buscarUsuario(usuarioId));
		return ResponseEntity.ok(response);
	}

	@Operation(
			summary = "Consultar meu perfil",
			description = "Retorna os dados cadastrais da conta do usuário autenticado no token JWT.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200",
					description = "Perfil retornado com sucesso",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
			@ApiResponse(
					responseCode = "401",
					description = "Não autenticado / Token ausente ou inválido",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Erro interno do servidor",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
	})
	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UsuarioResponse> buscarMeuPerfil(Authentication authentication) {
		String email = authentication.getName();
		UsuarioResponse response = usuarioService.toResponse(usuarioService.buscarPorEmail(email));
		return ResponseEntity.ok(response);
	}

	@Operation(
			summary = "Atualizar meus dados cadastrais",
			description = "Permite ao usuário autenticado atualizar seus próprios dados de cadastro.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200",
					description = "Dados atualizados com sucesso",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
			@ApiResponse(
					responseCode = "400",
					description = "Dados inválidos de cadastro",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "401",
					description = "Não autenticado / Token ausente ou inválido",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "403",
					description = "Proibido: tentativa de alterar dados de outro usuário",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Erro interno do servidor",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
	})
	@PatchMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UsuarioResponse> atualizarMeusDados(
			Authentication authentication,
			@Valid @RequestBody CadastroUsuarioRequest request) {
		String email = authentication.getName();
		
		if (!email.equals(request.email())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o proprietário pode atualizar seus próprios dados");
		}
		
		UsuarioResponse response = usuarioService.toResponse(usuarioService.buscarPorEmail(email));
		return ResponseEntity.ok(response);
	}
}

