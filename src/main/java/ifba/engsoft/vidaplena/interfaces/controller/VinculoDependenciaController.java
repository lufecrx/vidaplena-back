package ifba.engsoft.vidaplena.interfaces.controller;

import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaResponseDTO;
import ifba.engsoft.vidaplena.domain.service.familia.VinculoDependenciaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vinculos")
public class VinculoDependenciaController {

    @Autowired
    private VinculoDependenciaService vinculoDependenciaService;

    @PostMapping
    public ResponseEntity<VinculoDependenciaResponseDTO> criarVinculo(@Valid @RequestBody VinculoDependenciaRequestDTO dto) {
        VinculoDependenciaResponseDTO vinculo = vinculoDependenciaService.criarVinculo(dto);
        return new ResponseEntity<>(vinculo, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerVinculo(@PathVariable UUID id) {
        vinculoDependenciaService.inativarVinculo(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}