package ifba.engsoft.vidaplena.domain.repository.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    /**
     * Verifica choque de horário para um profissional.
     *
     * A lógica de verificação é:
     * (inicio_existente < :fim_proposto AND fim_existente > :inicio_proposto)
     *
     * Esta query garante que:
     * 1. Não há conflito quando uma consulta começa exatamente no momento em que outra termina
     * 2. Considera apenas agendamentos com status diferente de CANCELADO
     * 3. Detecta todos os casos de sobreposição de horários
     *
     * @param profissionalId ID do profissional
     * @param fimProposto Data/hora de término do agendamento proposto
     * @param inicioProposto Data/hora de início do agendamento proposto
     * @param statusCancelado Status que representa um agendamento cancelado
     * @return Lista de agendamentos conflitantes
     */
    @Query("SELECT a FROM Agendamento a WHERE " +
           "a.profissional.id = :profissionalId AND " +
           "a.status != :statusCancelado AND " +
           "a.dataHoraInicio < :fimProposto AND " +
           "a.dataHoraFim > :inicioProposto")
    List<Agendamento> verificarChoqueHorario(
            @Param("profissionalId") UUID profissionalId,
            @Param("fimProposto") LocalDateTime fimProposto,
            @Param("inicioProposto") LocalDateTime inicioProposto,
            @Param("statusCancelado") StatusAgendamento statusCancelado);
}