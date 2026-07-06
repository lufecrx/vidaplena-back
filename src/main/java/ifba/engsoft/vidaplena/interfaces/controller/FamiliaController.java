package ifba.engsoft.vidaplena.interfaces.controller;

import ifba.engsoft.vidaplena.domain.dto.familia.FamiliaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.FamiliaResponseDTO;
import ifba.engsoft.vidaplena.domain.service.familia.FamiliaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/familias")
public class FamiliaController {

    @Autowired
    private FamiliaService familiaService;

    @PostMapping
    public ResponseEntity<FamiliaResponseDTO> criarFamilia(@Valid @RequestBody FamiliaRequestDTO dto) {
        FamiliaResponseDTO familia = familiaService.criarFamilia(dto);
        return new ResponseEntity<>(familia, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FamiliaResponseDTO> buscarFamiliaPorId(@PathVariable UUID id) {
        // TODO: Implementar a lógica para buscar uma família por ID
        return new ResponseEntity<>(HttpStatus.OK);
    }
}