package ifba.engsoft.vidaplena.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ifba.engsoft.vidaplena.domain.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByEmailIgnoreCase(String email);

	Optional<Usuario> findByCpf(String cpf);

	boolean existsByEmailIgnoreCaseOrCpf(String email, String cpf);
}