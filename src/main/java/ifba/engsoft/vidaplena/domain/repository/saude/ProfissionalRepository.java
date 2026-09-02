package ifba.engsoft.vidaplena.domain.repository.saude;

import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, UUID> {
    
    @EntityGraph(attributePaths = {"usuario"})
    @Query("SELECT p FROM Profissional p")
    List<Profissional> findAllComUsuario();

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    List<Profissional> findAll();

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Optional<Profissional> findById(UUID id);

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Profissional> findByUsuario(Usuario usuario);
    
    boolean existsByUsuario(Usuario usuario);
    
    boolean existsByRegistroConselho(String registroConselho);
    
    Optional<Profissional> findByRegistroConselho(String registroConselho);

    long countByClinicaId(UUID clinicaId);

    java.util.List<Profissional> findByClinicaId(UUID clinicaId);
}