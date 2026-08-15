package ifba.engsoft.vidaplena.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ifba.engsoft.vidaplena.domain.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByEmailIgnoreCase(String email);

	Optional<Usuario> findByCpf(String cpf);

	boolean existsByEmail(String email);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseOrCpf(String email, String cpf);

	long countByEmpresaId(UUID empresaId);

	java.util.List<Usuario> findByEmpresaId(UUID empresaId);

	java.util.List<Usuario> findByClinicaId(UUID clinicaId);
}