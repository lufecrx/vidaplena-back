package ifba.engsoft.vidaplena.domain.repository.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Prontuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, UUID> {
    
    Prontuario findByPacienteId(UUID pacienteId);

    boolean existsByPacienteId(UUID pacienteId);
}