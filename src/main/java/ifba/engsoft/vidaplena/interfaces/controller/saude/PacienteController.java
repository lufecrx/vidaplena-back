package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.PacienteDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.PacienteResponseDTO;
import ifba.engsoft.vidaplena.domain.service.saude.PacienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "*")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> criarPaciente(@Valid @RequestBody PacienteDTO dto) {
        PacienteResponseDTO paciente = pacienteService.criarPaciente(dto);
        return new ResponseEntity<>(paciente, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> atualizarPaciente(@PathVariable UUID id, @Valid @RequestBody PacienteDTO dto) {
        PacienteResponseDTO paciente = pacienteService.atualizarPaciente(id, dto);
        return new ResponseEntity<>(paciente, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> obterPaciente(@PathVariable UUID id) {
        PacienteResponseDTO paciente = pacienteService.obterPaciente(id);
        return new ResponseEntity<>(paciente, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponseDTO>> listarPacientes() {
        List<PacienteResponseDTO> pacientes = pacienteService.listarPacientes();
        return new ResponseEntity<>(pacientes, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPaciente(@PathVariable UUID id) {
        pacienteService.deletarPaciente(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}