package ifba.engsoft.vidaplena.domain.repository.saude;

import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, UUID> {
    
    Optional<Profissional> findByUsuario(Usuario usuario);
    
    boolean existsByUsuario(Usuario usuario);
    
    boolean existsByRegistroConselho(String registroConselho);
    
    Optional<Profissional> findByRegistroConselho(String registroConselho);
}