package ifba.engsoft.vidaplena.domain.repository.saude;

import ifba.engsoft.vidaplena.domain.model.saude.RegistroAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegistroAtendimentoRepository extends JpaRepository<RegistroAtendimento, UUID> {
    
    @Query("SELECT r FROM RegistroAtendimento r WHERE r.prontuario.id = :prontuarioId")
    List<RegistroAtendimento> findAllByProntuarioId(@Param("prontuarioId") UUID prontuarioId);

    List<RegistroAtendimento> findAllByProntuarioIdOrderByDataRegistroAsc(UUID prontuarioId);

    boolean existsByAgendamentoId(UUID agendamentoId);

    Optional<RegistroAtendimento> findByAgendamentoId(UUID agendamentoId);
}