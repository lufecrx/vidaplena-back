package ifba.engsoft.vidaplena.domain.service.familia;

import ifba.engsoft.vidaplena.domain.dto.familia.FamiliaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.FamiliaResponseDTO;
import ifba.engsoft.vidaplena.domain.model.familia.Familia;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.FamiliaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FamiliaService {

    @Autowired
    private FamiliaRepository familiaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public FamiliaResponseDTO criarFamilia(FamiliaRequestDTO dto) {
        // Verificar se todos os IDs de usuários existem
        List<UUID> membrosIds = dto.membros();
        if (membrosIds != null && !membrosIds.isEmpty()) {
            for (UUID id : membrosIds) {
                if (!usuarioRepository.existsById(id)) {
                    throw new RegraNegocioException("Usuário com ID " + id + " não encontrado");
                }
            }
        }

        // Criar a família
        Familia familia = new Familia(dto.nome());
        familia.setMembros(usuarioRepository.findAllById(membrosIds));

        // Salvar no banco de dados
        Familia familiaSalva = familiaRepository.save(familia);

        // Mapear para DTO de resposta
        return new FamiliaResponseDTO(
                familiaSalva.getId(),
                familiaSalva.getNome(),
                familiaSalva.getMembros().stream()
                        .map(usuario -> new FamiliaResponseDTO.MembroResumo(usuario.getId(), usuario.getNome()))
                        .collect(Collectors.toList())
        );
    }
}