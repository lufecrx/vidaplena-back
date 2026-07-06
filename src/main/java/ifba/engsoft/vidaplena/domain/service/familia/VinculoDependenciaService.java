package ifba.engsoft.vidaplena.domain.service.familia;

import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaResponseDTO;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.familia.VinculoDependencia;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.VinculoDependenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class VinculoDependenciaService {

    @Autowired
    private VinculoDependenciaRepository vinculoDependenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public VinculoDependenciaResponseDTO criarVinculo(VinculoDependenciaRequestDTO dto) {
        // Regra 1: Um usuário NÃO pode ser dependente dele mesmo
        if (dto.responsavelId().equals(dto.dependenteId())) {
            throw new RegraNegocioException("Um usuário não pode ser dependente de si mesmo");
        }

        // Regra 2: A data de início não pode ser uma data futura
        if (dto.dataInicio() != null && dto.dataInicio().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data de início do vínculo não pode ser uma data futura");
        }

        // Regra 3: Buscar o responsável e o dependente no UsuarioRepository
        Usuario responsavel = usuarioRepository.findById(dto.responsavelId())
                .orElseThrow(() -> new RegraNegocioException("Usuário responsável com ID " + dto.responsavelId() + " não encontrado"));

        Usuario dependente = usuarioRepository.findById(dto.dependenteId())
                .orElseThrow(() -> new RegraNegocioException("Usuário dependente com ID " + dto.dependenteId() + " não encontrado"));

        // Criar o vínculo de dependência
        VinculoDependencia vinculo = new VinculoDependencia();
        vinculo.setTipo(dto.tipo());
        vinculo.setDataInicio(dto.dataInicio());
        vinculo.setResponsavel(responsavel);
        vinculo.setDependente(dependente);

        // Salvar o vínculo
        VinculoDependencia vinculoSalvo = vinculoDependenciaRepository.save(vinculo);

        // Mapear para DTO de resposta
        return new VinculoDependenciaResponseDTO(
                vinculoSalvo.getId(),
                vinculoSalvo.getResponsavel().getNome(),
                vinculoSalvo.getDependente().getNome(),
                vinculoSalvo.getTipo(),
                vinculoSalvo.getDataInicio()
        );
    }
}