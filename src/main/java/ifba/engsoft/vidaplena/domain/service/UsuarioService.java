package ifba.engsoft.vidaplena.domain.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.CadastroUsuarioRequest;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.UsuarioResponse;

@Service
@Transactional
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public UsuarioResponse criarUsuario(CadastroUsuarioRequest request, Set<TipoUsuario> tipos, StatusUsuario status) {
		String emailNormalizado = normalizarEmail(request.email());
		String cpfNormalizado = normalizarCpf(request.cpf());
		if (usuarioRepository.existsByEmailIgnoreCaseOrCpf(emailNormalizado, cpfNormalizado)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ou CPF já cadastrados");
		}

		Usuario usuario = new Usuario(
				request.nome(),
				cpfNormalizado,
				emailNormalizado,
				passwordEncoder.encode(request.senha()),
				request.telefone(),
				request.dataNascimento(),
				status,
				tipos == null ? new HashSet<>() : new HashSet<>(tipos));

		return toResponse(usuarioRepository.save(usuario));
	}

	public UsuarioResponse atualizarStatus(UUID usuarioId, StatusUsuario status) {
		Usuario usuario = buscarUsuario(usuarioId);
		usuario.atualizarStatus(status);
		return toResponse(usuario);
	}

	public UsuarioResponse atualizarTipos(UUID usuarioId, Set<TipoUsuario> tipos) {
		Usuario usuario = buscarUsuario(usuarioId);
		usuario.substituirTipos(tipos);
		return toResponse(usuario);
	}

	public UsuarioResponse atualizarSenha(UUID usuarioId, String novaSenha) {
		Usuario usuario = buscarUsuario(usuarioId);
		usuario.atualizarSenha(passwordEncoder.encode(novaSenha));
		return toResponse(usuario);
	}

	@Transactional(readOnly = true)
	public Usuario buscarUsuario(UUID usuarioId) {
		return usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
	}

	@Transactional(readOnly = true)
	public Usuario buscarPorEmail(String email) {
		return usuarioRepository.findByEmailIgnoreCase(normalizarEmail(email))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
	}

	@Transactional(readOnly = true)
	public UsuarioResponse toResponse(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNome(),
				usuario.getCpf(),
				usuario.getEmail(),
				usuario.getTelefone(),
				usuario.getDataNascimento(),
				usuario.getStatus(),
				Set.copyOf(usuario.getTipos()));
	}

	private String normalizarEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

	private String normalizarCpf(String cpf) {
		return cpf == null ? null : cpf.replaceAll("\\D", "");
	}
}