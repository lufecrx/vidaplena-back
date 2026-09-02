package ifba.engsoft.vidaplena.domain.service.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.ProfissionalDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.ProfissionalResponseDTO;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import ifba.engsoft.vidaplena.domain.repository.saude.ProfissionalRepository;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.service.familia.RegraNegocioException;
import ifba.engsoft.vidaplena.interfaces.dto.usuario.UsuarioResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProfissionalService {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public ProfissionalResponseDTO criarProfissional(ProfissionalDTO dto) {
        // Validar se o usuário existe
        Usuario usuario = usuarioRepository.findById(UUID.fromString(dto.usuarioId()))
                .orElseThrow(() -> new RegraNegocioException("Usuário com ID " + dto.usuarioId() + " não encontrado"));

        // Verificar se já existe um profissional para este usuário
        if (profissionalRepository.existsByUsuario(usuario)) {
            throw new RegraNegocioException("Já existe um profissional cadastrado para este usuário");
        }

        // Verificar se o registro de conselho já existe
        if (dto.registroConselho() != null && !dto.registroConselho().isEmpty()) {
            if (profissionalRepository.existsByRegistroConselho(dto.registroConselho())) {
                throw new RegraNegocioException("Registro de conselho já cadastrado para outro profissional");
            }
        }

        // Criar o profissional
        Profissional profissional = new Profissional(usuario);
        profissional.setRegistroConselho(dto.registroConselho());
        profissional.setEspecialidade(dto.especialidade());
        profissional.setClinicaId(dto.clinicaId());

        Profissional profissionalSalvo = profissionalRepository.save(profissional);
        
        return mapearParaResponseDTO(profissionalSalvo);
    }

    public ProfissionalResponseDTO atualizarProfissional(UUID id, ProfissionalDTO dto) {
        // Verificar se o profissional existe
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Profissional com ID " + id + " não encontrado"));

        // Validar se o usuário existe
        Usuario usuario = usuarioRepository.findById(UUID.fromString(dto.usuarioId()))
                .orElseThrow(() -> new RegraNegocioException("Usuário com ID " + dto.usuarioId() + " não encontrado"));

        // Verificar se o usuário já está associado a outro profissional (exceto ao próprio)
        Profissional profissionalComUsuario = profissionalRepository.findByUsuario(usuario).orElse(null);
        if (profissionalComUsuario != null && !profissionalComUsuario.getId().equals(id)) {
            throw new RegraNegocioException("Este usuário já está associado a outro profissional");
        }

        // Verificar se o registro de conselho já existe para outro profissional
        if (dto.registroConselho() != null && !dto.registroConselho().isEmpty()) {
            Profissional profissionalComRegistro = profissionalRepository.findByRegistroConselho(dto.registroConselho()).orElse(null);
            if (profissionalComRegistro != null && !profissionalComRegistro.getId().equals(id)) {
                throw new RegraNegocioException("Registro de conselho já cadastrado para outro profissional");
            }
        }

        // Atualizar o profissional
        profissional.setUsuario(usuario);
        profissional.setRegistroConselho(dto.registroConselho());
        profissional.setEspecialidade(dto.especialidade());
        profissional.setClinicaId(dto.clinicaId());

        Profissional profissionalAtualizado = profissionalRepository.save(profissional);
        
        return mapearParaResponseDTO(profissionalAtualizado);
    }

    public ProfissionalResponseDTO obterProfissional(UUID id) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Profissional com ID " + id + " não encontrado"));
        
        return mapearParaResponseDTO(profissional);
    }

    public ProfissionalResponseDTO obterPorUsuarioId(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Usuário com ID " + usuarioId + " não encontrado"));

        Profissional profissional = profissionalRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RegraNegocioException("Perfil clínico de profissional não encontrado para o usuário informado"));

        return mapearParaResponseDTO(profissional);
    }

    public List<ProfissionalResponseDTO> listarProfissionais() {
        return profissionalRepository.findAllComUsuario().stream()
                .map(this::mapearParaResponseDTO)
                .collect(Collectors.toList());
    }

    public void deletarProfissional(UUID id) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Profissional com ID " + id + " não encontrado"));
        
        profissionalRepository.delete(profissional);
    }

    private ProfissionalResponseDTO mapearParaResponseDTO(Profissional profissional) {
        UsuarioResponse usuarioResponse = profissional.getUsuario() != null
                ? UsuarioResponse.from(profissional.getUsuario())
                : null;

        String usuarioId = profissional.getUsuario() != null && profissional.getUsuario().getId() != null
                ? profissional.getUsuario().getId().toString()
                : null;

        return new ProfissionalResponseDTO(
                profissional.getId(),
                usuarioId,
                profissional.getRegistroConselho(),
                profissional.getEspecialidade(),
                profissional.getClinicaId(),
                usuarioResponse
        );
    }
}