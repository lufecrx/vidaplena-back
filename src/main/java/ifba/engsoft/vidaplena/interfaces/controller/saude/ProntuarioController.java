package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.ProntuarioDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.ProntuarioResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.RegistroAtendimentoResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.NotaRetificacaoResponseDTO;
import ifba.engsoft.vidaplena.domain.model.saude.Prontuario;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.model.saude.RegistroAtendimento;
import ifba.engsoft.vidaplena.domain.model.saude.NotaRetificacao;
import ifba.engsoft.vidaplena.domain.service.saude.ProntuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prontuarios")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR')")
public class ProntuarioController {

    @Autowired
    private ProntuarioService prontuarioService;

    @PostMapping
    public ResponseEntity<ProntuarioResponseDTO> criarProntuario(@Valid @RequestBody ProntuarioDTO prontuarioDTO) {
        Prontuario prontuario = new Prontuario();
        
        Paciente paciente = new Paciente();
        paciente.setId(prontuarioDTO.pacienteId());
        prontuario.setPaciente(paciente);
        prontuario.setObservacoesGerais(prontuarioDTO.observacoesGerais());
        
        Prontuario novoProntuario = prontuarioService.criarProntuario(prontuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponseDTO(novoProntuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProntuarioResponseDTO> obterProntuario(@PathVariable UUID id) {
        Prontuario prontuario = prontuarioService.obterProntuarioPorId(id);
        return ResponseEntity.ok(mapToResponseDTO(prontuario));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<ProntuarioResponseDTO> obterProntuarioPorPacienteId(@PathVariable UUID pacienteId) {
        Prontuario prontuario = prontuarioService.obterProntuarioPorPacienteId(pacienteId);
        return ResponseEntity.ok(mapToResponseDTO(prontuario));
    }

    @GetMapping("/paciente/{pacienteId}/historico")
    public ResponseEntity<List<RegistroAtendimentoResponseDTO>> obterHistoricoClinicoPaciente(@PathVariable UUID pacienteId) {
        List<RegistroAtendimento> registros = prontuarioService.obterHistoricoClinicoPaciente(pacienteId);
        List<RegistroAtendimentoResponseDTO> response = registros.stream()
                .map(this::mapToRegistroResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    // Mappings Helpers
    private ProntuarioResponseDTO mapToResponseDTO(Prontuario prontuario) {
        List<RegistroAtendimentoResponseDTO> registros = prontuario.getRegistrosAtendimento().stream()
                .map(this::mapToRegistroResponseDTO)
                .toList();

        return new ProntuarioResponseDTO(
                prontuario.getId(),
                prontuario.getPaciente().getId(),
                prontuario.getPaciente().getUsuario() != null ? prontuario.getPaciente().getUsuario().getNome() : "N/A",
                prontuario.getObservacoesGerais(),
                registros
        );
    }

    private RegistroAtendimentoResponseDTO mapToRegistroResponseDTO(RegistroAtendimento registro) {
        List<NotaRetificacaoResponseDTO> notas = registro.getNotasRetificacao().stream()
                .map(this::mapToNotaResponseDTO)
                .toList();

        return new RegistroAtendimentoResponseDTO(
                registro.getId(),
                registro.getProntuario().getId(),
                registro.getProfissional().getId(),
                registro.getProfissional().getUsuario() != null ? registro.getProfissional().getUsuario().getNome() : "N/A",
                registro.getProfissional().getRegistroConselho(),
                registro.getAgendamento().getId(),
                registro.getDataRegistro(),
                registro.getSintomasRelatados(),
                registro.getDiagnostico(),
                registro.getPrescricaoMedica(),
                registro.getPrescricaoEnfermagem(),
                registro.getNotasClinicas(),
                registro.isFinalizado(),
                notas
        );
    }

    private NotaRetificacaoResponseDTO mapToNotaResponseDTO(NotaRetificacao nota) {
        return new NotaRetificacaoResponseDTO(
                nota.getId(),
                nota.getRegistroAtendimento().getId(),
                nota.getProfissional().getId(),
                nota.getProfissional().getUsuario() != null ? nota.getProfissional().getUsuario().getNome() : "N/A",
                nota.getProfissional().getRegistroConselho(),
                nota.getDataRegistro(),
                nota.getTexto()
        );
    }
}