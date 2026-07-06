package ifba.engsoft.vidaplena.interfaces.controller;

import ifba.engsoft.vidaplena.domain.dto.organizacao.EmpresaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.EmpresaResponseDTO;
import ifba.engsoft.vidaplena.domain.service.organizacao.OrganizacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    @Autowired
    private OrganizacaoService organizacaoService;

    @PostMapping
    public ResponseEntity<EmpresaResponseDTO> criarEmpresa(@Valid @RequestBody EmpresaRequestDTO dto) {
        EmpresaResponseDTO empresa = organizacaoService.criarEmpresa(dto);
        return new ResponseEntity<>(empresa, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> buscarEmpresaPorId(@PathVariable UUID id) {
        // TODO: Implementar a lógica para buscar uma empresa por ID
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Void> listarEmpresas() {
        // TODO: Implementar a lógica para listar todas as empresas
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
