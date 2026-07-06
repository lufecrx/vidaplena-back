package ifba.engsoft.vidaplena.interfaces.controller;

import ifba.engsoft.vidaplena.domain.dto.organizacao.ClinicaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.ClinicaResponseDTO;
import ifba.engsoft.vidaplena.domain.service.organizacao.OrganizacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clinicas")
public class ClinicaController {

    @Autowired
    private OrganizacaoService organizacaoService;

    @PostMapping
    public ResponseEntity<ClinicaResponseDTO> criarClinica(@Valid @RequestBody ClinicaRequestDTO dto) {
        ClinicaResponseDTO clinica = organizacaoService.criarClinica(dto);
        return new ResponseEntity<>(clinica, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicaResponseDTO> buscarClinicaPorId(@PathVariable UUID id) {
        // TODO: Implementar a lógica para buscar uma clínica por ID
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Void> listarClinicas() {
        // Aqui poderíamos adicionar lógica para listar as clínicas
        // Por enquanto, vamos apenas retornar um exemplo
        return new ResponseEntity<>(HttpStatus.OK);
    }
}