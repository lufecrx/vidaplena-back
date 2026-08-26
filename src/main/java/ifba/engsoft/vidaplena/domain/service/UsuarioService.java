package ifba.engsoft.vidaplena.domain.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	public UsuarioResponse atualizarDadosCadastrais(UUID usuarioId, String nome, String telefone, java.time.LocalDate dataNascimento) {
		Usuario usuario = buscarUsuario(usuarioId);
		if (nome != null && !nome.isBlank()) {
			usuario.setNome(nome.trim());
		}
		if (telefone != null) {
			usuario.setTelefone(telefone.trim());
		}
		if (dataNascimento != null) {
			usuario.setDataNascimento(dataNascimento);
		}
		return toResponse(usuarioRepository.save(usuario));
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

	public void deletarUsuario(UUID usuarioId) {
		Usuario usuario = buscarUsuario(usuarioId);
		usuarioRepository.delete(usuario);
	}

	@Transactional(readOnly = true)
	public Page<UsuarioResponse> listarUsuarios(Pageable pageable) {
		return usuarioRepository.findAll(pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Usuario buscarUsuario(UUID usuarioId) {
		return usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
	}

	@Transactional(readOnly = true)
	public Usuario buscarPorEmail(String email) {
		String emailNormalizado = normalizarEmail(email);
		if (emailNormalizado == null || emailNormalizado.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail informado é inválido");
		}
		return usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
	}

	@Transactional(readOnly = true)
	public Usuario buscarPorCpf(String cpf) {
		String cpfNormalizado = normalizarCpf(cpf);
		if (cpfNormalizado == null || cpfNormalizado.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF informado é inválido");
		}
		return usuarioRepository.findByCpf(cpfNormalizado)
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