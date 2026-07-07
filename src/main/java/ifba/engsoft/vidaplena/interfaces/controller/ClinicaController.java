package ifba.engsoft.vidaplena.interfaces.controller;

import ifba.engsoft.vidaplena.domain.dto.organizacao.ClinicaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.ClinicaResponseDTO;
import ifba.engsoft.vidaplena.domain.service.organizacao.OrganizacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clinicas")
public class ClinicaController {

    @Autowired
    private OrganizacaoService organizacaoService;

    /**
     * Criação de uma nova clínica.
     * Acesso restrito a: ADMINISTRADOR ou REPRESENTANTE_EMPRESA.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'REPRESENTANTE_EMPRESA')")
    public ResponseEntity<ClinicaResponseDTO> criarClinica(@Valid @RequestBody ClinicaRequestDTO dto) {
        ClinicaResponseDTO clinica = organizacaoService.criarClinica(dto);
        return new ResponseEntity<>(clinica, HttpStatus.CREATED);
    }

    /**
     * Visualização de dados de uma clínica específica.
     * Acesso permitido para: ADMINISTRADOR ou REPRESENTANTE_EMPRESA (vinculado à clínica).
     *
     * Nota: A anotação @PreAuthorize restringe por Role. A validação de vínculo com a clínica
     * deve ser realizada no serviço OrganizacaoService.buscarClinicaPorId() verificando se o ID do usuário logado
     * está vinculado à clínica solicitada (para REPRESENTANTE_EMPRESA).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'REPRESENTANTE_EMPRESA')")
    public ResponseEntity<ClinicaResponseDTO> buscarClinicaPorId(@PathVariable UUID id) {
        // TODO: No Service, validar se o usuário logado é REPRESENTANTE_EMPRESA vinculado à clínica
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Listagem de todas as clínicas.
     * Acesso restrito a: ADMINISTRADOR ou REPRESENTANTE_EMPRESA.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'REPRESENTANTE_EMPRESA')")
    public ResponseEntity<Void> listarClinicas() {
        // TODO: Implementar a lógica para listar todas as clínicas
        return new ResponseEntity<>(HttpStatus.OK);
    }
}