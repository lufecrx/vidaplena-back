package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.RegistroAtendimentoDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.RegistroAtendimentoResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.NotaRetificacaoDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.NotaRetificacaoResponseDTO;
import ifba.engsoft.vidaplena.domain.model.saude.*;
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
@RequestMapping("/api/registros-atendimento")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('MEDICO', 'NUTRICIONISTA', 'PERSONAL_TRAINER', 'ADMINISTRADOR')")
public class RegistroAtendimentoController {

    @Autowired
    private ProntuarioService prontuarioService;

    @PostMapping
    public ResponseEntity<RegistroAtendimentoResponseDTO> criarRegistroAtendimento(@Valid @RequestBody RegistroAtendimentoDTO dto) {
        RegistroAtendimento registro = new RegistroAtendimento();
        
        Prontuario prontuario = new Prontuario();
        prontuario.setId(dto.prontuarioId());
        registro.setProntuario(prontuario);

        Profissional profissional = new Profissional();
        profissional.setId(dto.profissionalId());
        registro.setProfissional(profissional);

        Agendamento agendamento = new Agendamento();
        agendamento.setId(dto.agendamentoId());
        registro.setAgendamento(agendamento);

        registro.setSintomasRelatados(dto.sintomasRelatados());
        registro.setDiagnostico(dto.diagnostico());
        registro.setPrescricaoMedica(dto.prescricaoMedica());
        registro.setPrescricaoEnfermagem(dto.prescricaoEnfermagem());
        registro.setNotasClinicas(dto.notasClinicas());
        registro.setFinalizado(dto.finalizado());

        RegistroAtendimento novoRegistro = prontuarioService.criarRegistroAtendimento(registro);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponseDTO(novoRegistro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistroAtendimentoResponseDTO> atualizarRegistroAtendimento(@PathVariable UUID id, @Valid @RequestBody RegistroAtendimentoDTO dto) {
        RegistroAtendimento registro = new RegistroAtendimento();
        registro.setSintomasRelatados(dto.sintomasRelatados());
        registro.setDiagnostico(dto.diagnostico());
        registro.setPrescricaoMedica(dto.prescricaoMedica());
        registro.setPrescricaoEnfermagem(dto.prescricaoEnfermagem());
        registro.setNotasClinicas(dto.notasClinicas());
        registro.setFinalizado(dto.finalizado());

        RegistroAtendimento registroAtualizado = prontuarioService.atualizarRegistroAtendimento(id, registro);
        return ResponseEntity.ok(mapToResponseDTO(registroAtualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRegistroAtendimento(@PathVariable UUID id) {
        prontuarioService.deletarRegistroAtendimento(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/retificar")
    public ResponseEntity<NotaRetificacaoResponseDTO> adicionarNotaRetificacao(@PathVariable UUID id, @Valid @RequestBody NotaRetificacaoDTO dto) {
        NotaRetificacao nota = prontuarioService.adicionarNotaRetificacao(id, dto.profissionalId(), dto.texto());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToNotaResponseDTO(nota));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistroAtendimentoResponseDTO> obterRegistroAtendimento(@PathVariable UUID id) {
        RegistroAtendimento registro = prontuarioService.obterRegistroAtendimentoPorId(id);
        return ResponseEntity.ok(mapToResponseDTO(registro));
    }

    @GetMapping("/prontuario/{prontuarioId}")
    public ResponseEntity<List<RegistroAtendimentoResponseDTO>> obterRegistrosPorProntuario(@PathVariable UUID prontuarioId) {
        List<RegistroAtendimento> registros = prontuarioService.obterRegistrosAtendimentoPorProntuario(prontuarioId);
        List<RegistroAtendimentoResponseDTO> response = registros.stream()
                .map(this::mapToResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    // Helper mappings
    private RegistroAtendimentoResponseDTO mapToResponseDTO(RegistroAtendimento registro) {
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