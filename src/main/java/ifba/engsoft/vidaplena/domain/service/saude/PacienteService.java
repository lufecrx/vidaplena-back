package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.PacienteDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.PacienteResponseDTO;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.repository.saude.PacienteRepository;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.service.familia.RegraNegocioException;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.UsuarioResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public PacienteResponseDTO criarPaciente(PacienteDTO dto) {
        // Validar se o usuário existe
        Usuario usuario = usuarioRepository.findById(UUID.fromString(dto.usuarioId()))
                .orElseThrow(() -> new RegraNegocioException("Usuário com ID " + dto.usuarioId() + " não encontrado"));

        // Verificar se já existe um paciente para este usuário
        if (pacienteRepository.existsByUsuario(usuario)) {
            throw new RegraNegocioException("Já existe um paciente cadastrado para este usuário");
        }

        // Criar o paciente
        Paciente paciente = new Paciente(usuario);
        paciente.setTipoSanguineo(dto.tipoSanguineo());
        paciente.setAlergias(dto.alergias() != null ? new ArrayList<>(dto.alergias()) : new ArrayList<>());
        paciente.setMedicamentosContinuos(dto.medicamentosContinuos() != null ? new ArrayList<>(dto.medicamentosContinuos()) : new ArrayList<>());
        paciente.setHistoricoFamiliar(dto.historicoFamiliar());

        Paciente pacienteSalvo = pacienteRepository.save(paciente);
        
        return mapearParaResponseDTO(pacienteSalvo);
    }

    public PacienteResponseDTO atualizarPaciente(UUID id, PacienteDTO dto) {
        // Verificar se o paciente existe
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Paciente com ID " + id + " não encontrado"));

        // Validar se o usuário existe
        Usuario usuario = usuarioRepository.findById(UUID.fromString(dto.usuarioId()))
                .orElseThrow(() -> new RegraNegocioException("Usuário com ID " + dto.usuarioId() + " não encontrado"));

        // Verificar se o usuário já está associado a outro paciente (exceto ao próprio)
        Paciente pacienteComUsuario = pacienteRepository.findByUsuario(usuario).orElse(null);
        if (pacienteComUsuario != null && !pacienteComUsuario.getId().equals(id)) {
            throw new RegraNegocioException("Este usuário já está associado a outro paciente");
        }

        // Atualizar o paciente
        paciente.setUsuario(usuario);
        paciente.setTipoSanguineo(dto.tipoSanguineo());
        paciente.setAlergias(dto.alergias() != null ? new ArrayList<>(dto.alergias()) : new ArrayList<>());
        paciente.setMedicamentosContinuos(dto.medicamentosContinuos() != null ? new ArrayList<>(dto.medicamentosContinuos()) : new ArrayList<>());
        paciente.setHistoricoFamiliar(dto.historicoFamiliar());

        Paciente pacienteAtualizado = pacienteRepository.save(paciente);
        
        return mapearParaResponseDTO(pacienteAtualizado);
    }

    public PacienteResponseDTO obterPaciente(UUID id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Paciente com ID " + id + " não encontrado"));
        
        return mapearParaResponseDTO(paciente);
    }

    public PacienteResponseDTO obterPorUsuarioId(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Usuário com ID " + usuarioId + " não encontrado"));

        Paciente paciente = pacienteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RegraNegocioException("Perfil clínico de paciente não encontrado para o usuário informado"));

        return mapearParaResponseDTO(paciente);
    }

    public List<PacienteResponseDTO> listarPacientes() {
        return pacienteRepository.findAll().stream()
                .map(this::mapearParaResponseDTO)
                .collect(Collectors.toList());
    }

    public void deletarPaciente(UUID id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Paciente com ID " + id + " não encontrado"));
        
        pacienteRepository.delete(paciente);
    }

    private PacienteResponseDTO mapearParaResponseDTO(Paciente paciente) {
        List<String> alergias = paciente.getAlergias() != null
                ? new ArrayList<>(paciente.getAlergias())
                : new ArrayList<>();

        List<String> medicamentosContinuos = paciente.getMedicamentosContinuos() != null
                ? new ArrayList<>(paciente.getMedicamentosContinuos())
                : new ArrayList<>();

        String usuarioId = paciente.getUsuario() != null && paciente.getUsuario().getId() != null
                ? paciente.getUsuario().getId().toString()
                : null;

        UsuarioResponse usuarioResponse = paciente.getUsuario() != null
                ? UsuarioResponse.from(paciente.getUsuario())
                : null;

        return new PacienteResponseDTO(
                paciente.getId(),
                usuarioId,
                paciente.getTipoSanguineo(),
                alergias,
                medicamentosContinuos,
                paciente.getHistoricoFamiliar(),
                usuarioResponse
        );
    }
}