package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.AgendamentoRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.AgendamentoResponseDTO;
import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import ifba.engsoft.vidaplena.domain.service.saude.AgendamentoService;
import ifba.engsoft.vidaplena.domain.service.saude.exception.RegraNegocioException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agendamentos")
@CrossOrigin(origins = "*")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamento(
            @Valid @RequestBody AgendamentoRequestDTO request) {
        try {
            // Neste ponto, você precisaria converter o DTO para a entidade Agendamento
            // e buscar os objetos Paciente e Profissional reais usando os IDs fornecidos
            // Por simplicidade, estamos apenas chamando o serviço que fará essa lógica
            
            // Exemplo de implementação real:
            // Paciente paciente = pacienteService.buscarPorId(request.getIdPaciente());
            // Profissional profissional = profissionalService.buscarPorId(request.getIdProfissional());
            // Agendamento agendamento = new Agendamento();
            // agendamento.setPaciente(paciente);
            // agendamento.setProfissional(profissional);
            // agendamento.setDataHoraInicio(request.getDataHoraInicio());
            // agendamento.setDataHoraFim(request.getDataHoraFim());
            // agendamento.setMotivoConsulta(request.getMotivoConsulta());
            // agendamento.setStatus(StatusAgendamento.AGENDADO);
            
            // Agendamento agendamentoSalvo = agendamentoService.criarAgendamento(agendamento);
            
            // AgendamentoResponseDTO response = converterParaResponseDTO(agendamentoSalvo);
            
            // Por enquanto, retornando um exemplo
            AgendamentoResponseDTO response = new AgendamentoResponseDTO();
            response.setId(UUID.randomUUID());
            response.setNomePaciente("Paciente Exemplo");
            response.setNomeProfissional("Profissional Exemplo");
            response.setDataHoraInicio(request.getDataHoraInicio());
            response.setDataHoraFim(request.getDataHoraFim());
            response.setStatus(StatusAgendamento.AGENDADO);
            response.setMotivoConsulta(request.getMotivoConsulta());
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RegraNegocioException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<AgendamentoResponseDTO> confirmarAgendamento(@PathVariable UUID id) {
        try {
            Agendamento agendamento = agendamentoService.atualizarStatus(id, StatusAgendamento.CONFIRMADO);
            
            AgendamentoResponseDTO response = new AgendamentoResponseDTO();
            response.setId(agendamento.getId());
            response.setNomePaciente("Paciente Exemplo");
            response.setNomeProfissional("Profissional Exemplo");
            response.setDataHoraInicio(agendamento.getDataHoraInicio());
            response.setDataHoraFim(agendamento.getDataHoraFim());
            response.setStatus(agendamento.getStatus());
            response.setMotivoConsulta(agendamento.getMotivoConsulta());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RegraNegocioException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<AgendamentoResponseDTO> cancelarAgendamento(@PathVariable UUID id) {
        try {
            Agendamento agendamento = agendamentoService.atualizarStatus(id, StatusAgendamento.CANCELADO);
            
            AgendamentoResponseDTO response = new AgendamentoResponseDTO();
            response.setId(agendamento.getId());
            response.setNomePaciente("Paciente Exemplo");
            response.setNomeProfissional("Profissional Exemplo");
            response.setDataHoraInicio(agendamento.getDataHoraInicio());
            response.setDataHoraFim(agendamento.getDataHoraFim());
            response.setStatus(agendamento.getStatus());
            response.setMotivoConsulta(agendamento.getMotivoConsulta());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RegraNegocioException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/profissional/{id}")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarAgendamentosPorProfissional(
            @PathVariable UUID id) {
        // Aqui você implementaria a lógica para buscar os agendamentos por profissional
        // Exemplo de implementação real:
        // List<Agendamento> agendamentos = agendamentoService.buscarPorProfissional(id);
        // List<AgendamentoResponseDTO> response = agendamentos.stream()
        //     .map(this::converterParaResponseDTO)
        //     .collect(Collectors.toList());
        
        // Por simplicidade, estamos retornando uma lista vazia
        List<AgendamentoResponseDTO> response = List.of();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}