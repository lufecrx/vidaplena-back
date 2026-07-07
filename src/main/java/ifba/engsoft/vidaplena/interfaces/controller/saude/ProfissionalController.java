package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.ProfissionalDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.ProfissionalResponseDTO;
import ifba.engsoft.vidaplena.domain.service.saude.ProfissionalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profissionais")
@CrossOrigin(origins = "*")
public class ProfissionalController {

    @Autowired
    private ProfissionalService profissionalService;

    @PostMapping
    public ResponseEntity<ProfissionalResponseDTO> criarProfissional(@Valid @RequestBody ProfissionalDTO dto) {
        ProfissionalResponseDTO profissional = profissionalService.criarProfissional(dto);
        return new ResponseEntity<>(profissional, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalResponseDTO> atualizarProfissional(@PathVariable UUID id, @Valid @RequestBody ProfissionalDTO dto) {
        ProfissionalResponseDTO profissional = profissionalService.atualizarProfissional(id, dto);
        return new ResponseEntity<>(profissional, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalResponseDTO> obterProfissional(@PathVariable UUID id) {
        ProfissionalResponseDTO profissional = profissionalService.obterProfissional(id);
        return new ResponseEntity<>(profissional, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ProfissionalResponseDTO>> listarProfissionais() {
        List<ProfissionalResponseDTO> profissionais = profissionalService.listarProfissionais();
        return new ResponseEntity<>(profissionais, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProfissional(@PathVariable UUID id) {
        profissionalService.deletarProfissional(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}