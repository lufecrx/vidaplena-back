package ifba.engsoft.vidaplena.domain.repository.saude;

import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    /**
     * Busca agendamentos associados a um profissional ordenados por data/hora asc.
     *
     * @param profissionalId ID do profissional
     * @return Lista de agendamentos do profissional ordenados cronologicamente
     */
    List<Agendamento> findByProfissionalIdOrderByDataHoraAsc(UUID profissionalId);

    /**
     * Verifica a existência de choque de horário ativo para o profissional na data/hora informada.
     *
     * @param profissionalId ID do profissional
     * @param dataHora Data e hora da consulta proposta
     * @param status Status a ser desconsiderado (ex: CANCELADO)
     * @return true se já existir agendamento ativo no mesmo horário, false caso contrário
     */
    boolean existsByProfissionalIdAndDataHoraAndStatusNot(
            UUID profissionalId,
            LocalDateTime dataHora,
            StatusAgendamento status
    );

    /**
     * Verifica choque de horário para um profissional com intervalo início e fim.
     */
    @Query("SELECT a FROM Agendamento a WHERE " +
           "a.profissional.id = :profissionalId AND " +
           "a.status != :statusCancelado AND " +
           "((a.dataHoraInicio IS NOT NULL AND a.dataHoraInicio < :fimProposto AND a.dataHoraFim > :inicioProposto) OR " +
           "(a.dataHora IS NOT NULL AND a.dataHora = :inicioProposto))")
    List<Agendamento> verificarChoqueHorario(
            @Param("profissionalId") UUID profissionalId,
            @Param("fimProposto") LocalDateTime fimProposto,
            @Param("inicioProposto") LocalDateTime inicioProposto,
            @Param("statusCancelado") StatusAgendamento statusCancelado);

    List<Agendamento> findByPacienteId(UUID pacienteId);

    boolean existsByPacienteIdAndProfissionalIdAndDataHora(UUID pacienteId, UUID profissionalId, LocalDateTime dataHora);

    boolean existsByPacienteIdAndProfissionalIdAndDataHoraInicio(UUID pacienteId, UUID profissionalId, LocalDateTime dataHoraInicio);
}