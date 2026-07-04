package ifba.engsoft.vidaplena.infrastructure.security;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

		return User.withUsername(usuario.getEmail())
				.password(usuario.getSenha())
				.authorities(usuario.getTipos().stream()
						.map(tipo -> new SimpleGrantedAuthority("ROLE_" + tipo.name()))
						.collect(Collectors.toSet()))
				.accountExpired(false)
				.accountLocked(false)
				.credentialsExpired(false)
				.disabled(usuario.getStatus().name().equals("INATIVO") || usuario.getStatus().name().equals("BLOQUEADO"))
				.build();
	}
}