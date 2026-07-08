package ifba.engsoft.vidaplena.interfaces.controller;

import ifba.engsoft.vidaplena.domain.dto.organizacao.EmpresaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.EmpresaResponseDTO;
import ifba.engsoft.vidaplena.domain.service.organizacao.OrganizacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    @Autowired
    private OrganizacaoService organizacaoService;

    /**
     * Criação de uma nova empresa.
     * Acesso restrito a: ADMINISTRADOR ou REPRESENTANTE_EMPRESA.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'REPRESENTANTE_EMPRESA')")
    public ResponseEntity<EmpresaResponseDTO> criarEmpresa(@Valid @RequestBody EmpresaRequestDTO dto) {
        EmpresaResponseDTO empresa = organizacaoService.criarEmpresa(dto);
        return new ResponseEntity<>(empresa, HttpStatus.CREATED);
    }

    /**
     * Visualização de dados de uma empresa específica.
     * Acesso permitido para: ADMINISTRADOR ou REPRESENTANTE_EMPRESA (vinculado à empresa).
     *
     * Nota: A anotação @PreAuthorize restringe por Role. A validação de vínculo com a empresa
     * deve ser realizada no serviço OrganizacaoService.buscarEmpresaPorId() verificando se o ID do usuário logado
     * está vinculado à empresa solicitada (para REPRESENTANTE_EMPRESA).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'REPRESENTANTE_EMPRESA')")
    public ResponseEntity<EmpresaResponseDTO> buscarEmpresaPorId(@PathVariable UUID id) {
        EmpresaResponseDTO empresa = organizacaoService.buscarEmpresaPorId(id);
        return new ResponseEntity<>(empresa, HttpStatus.OK);
    }

    /**
     * Listagem de todas as empresas.
     * Acesso restrito a: ADMINISTRADOR ou REPRESENTANTE_EMPRESA.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'REPRESENTANTE_EMPRESA')")
    public ResponseEntity<List<EmpresaResponseDTO>> listarEmpresas() {
        List<EmpresaResponseDTO> empresas = organizacaoService.listarEmpresas();
        return new ResponseEntity<>(empresas, HttpStatus.OK);
    }
}
