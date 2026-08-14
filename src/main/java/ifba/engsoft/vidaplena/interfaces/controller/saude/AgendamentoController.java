package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.CriarAgendamentoRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.AgendamentoResponseDTO;
import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import ifba.engsoft.vidaplena.domain.service.saude.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/agendamentos", "/api/agendamentos"})
@CrossOrigin(origins = "*")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMINISTRADOR', 'RESPONSAVEL', 'FUNCIONARIO_ADMINISTRATIVO', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'CUIDADOR')")
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamento(
            @Valid @RequestBody CriarAgendamentoRequestDTO request,
            UriComponentsBuilder uriBuilder) {
        AgendamentoResponseDTO response = agendamentoService.criarAgendamento(request);
        URI uri = uriBuilder.path("/api/v1/agendamentos/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/profissional/{profissionalId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR', 'FUNCIONARIO_ADMINISTRATIVO', 'PACIENTE', 'RESPONSAVEL')")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarAgendamentosPorProfissional(
            @PathVariable UUID profissionalId) {
        List<AgendamentoResponseDTO> response = agendamentoService.listarPorProfissional(profissionalId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMINISTRADOR', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'FUNCIONARIO_ADMINISTRATIVO')")
    public ResponseEntity<AgendamentoResponseDTO> confirmarAgendamento(@PathVariable UUID id) {
        Agendamento agendamento = agendamentoService.atualizarStatus(id, StatusAgendamento.CONFIRMADO);
        return ResponseEntity.ok(agendamentoService.mapearParaResponseDTO(agendamento));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMINISTRADOR', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'FUNCIONARIO_ADMINISTRATIVO', 'RESPONSAVEL')")
    public ResponseEntity<AgendamentoResponseDTO> cancelarAgendamento(@PathVariable UUID id) {
        Agendamento agendamento = agendamentoService.atualizarStatus(id, StatusAgendamento.CANCELADO);
        return ResponseEntity.ok(agendamentoService.mapearParaResponseDTO(agendamento));
    }
}