package ifba.engsoft.vidaplena.interfaces.controller;

import ifba.engsoft.vidaplena.domain.dto.familia.FamiliaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.FamiliaResponseDTO;
import ifba.engsoft.vidaplena.domain.service.familia.FamiliaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/familias")
public class FamiliaController {

    @Autowired
    private FamiliaService familiaService;

    /**
     * Criação de um novo núcleo familiar.
     * Acesso permitido para: ADMINISTRADOR ou RESPONSAVEL.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RESPONSAVEL')")
    public ResponseEntity<FamiliaResponseDTO> criarFamilia(@Valid @RequestBody FamiliaRequestDTO dto) {
        FamiliaResponseDTO familia = familiaService.criarFamilia(dto);
        return new ResponseEntity<>(familia, HttpStatus.CREATED);
    }

    /**
     * Visualização de dados de uma família específica e seus membros.
     * Acesso permitido para: ADMINISTRADOR, PROFISSIONAL (MEDICO, NUTRICIONISTA, PERSONAL_TRAINER, CUIDADOR, FUNCIONARIO_ADMINISTRATIVO)
     * ou RESPONSAVEL/PACIENTE (desde que pertencam à família - validação de vínculo deve ser feita no Service).
     *
     * Nota: A anotação @PreAuthorize restringe por Role. A validação de pertencimento ao núcleo familiar
     * deve ser realizada no serviço FamiliaService.buscarFamiliaPorId() verificando se o ID do usuário logado
     * está vinculado à família solicitada.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'CUIDADOR', 'FUNCIONARIO_ADMINISTRATIVO', 'RESPONSAVEL', 'PACIENTE')")
    public ResponseEntity<FamiliaResponseDTO> buscarFamiliaPorId(@PathVariable UUID id) {
        FamiliaResponseDTO familia = familiaService.buscarFamilia(id);
        return new ResponseEntity<>(familia, HttpStatus.OK);
    }
}