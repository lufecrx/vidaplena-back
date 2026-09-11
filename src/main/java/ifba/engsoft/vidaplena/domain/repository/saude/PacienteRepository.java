package ifba.engsoft.vidaplena.domain.repository.saude;

import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, UUID> {
    
    @Override
    @EntityGraph(attributePaths = {"usuario"})
    List<Paciente> findAll();

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Optional<Paciente> findById(UUID id);

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Paciente> findByUsuario(Usuario usuario);
    
    boolean existsByUsuario(Usuario usuario);
}