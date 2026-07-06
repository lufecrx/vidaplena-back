package ifba.engsoft.vidaplena.domain.service.organizacao;

import ifba.engsoft.vidaplena.domain.dto.organizacao.ClinicaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.ClinicaResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.EmpresaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.EmpresaResponseDTO;
import ifba.engsoft.vidaplena.domain.model.organizacao.Clinica;
import ifba.engsoft.vidaplena.domain.model.organizacao.Empresa;
import ifba.engsoft.vidaplena.domain.model.organizacao.Organizacao;
import ifba.engsoft.vidaplena.domain.repository.organizacao.OrganizacaoRepository;
import ifba.engsoft.vidaplena.domain.service.familia.RegraNegocioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrganizacaoService {

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    public EmpresaResponseDTO criarEmpresa(EmpresaRequestDTO dto) {
        // Verificar se o CNPJ já existe
        Optional<Organizacao> organizacaoExistente = organizacaoRepository.findByCnpj(dto.cnpj());
        if (organizacaoExistente.isPresent()) {
            throw new RegraNegocioException("Já existe uma organização cadastrada com o CNPJ informado");
        }

        // Criar a empresa
        Empresa empresa = new Empresa(dto.nome(), dto.cnpj(), dto.setor());

        // Salvar no banco de dados
        Empresa empresaSalva = (Empresa) organizacaoRepository.save(empresa);

        // Mapear para DTO de resposta
        return new EmpresaResponseDTO(
                empresaSalva.getId(),
                empresaSalva.getNome(),
                empresaSalva.getCnpj(),
                empresaSalva.getSetor()
        );
    }

    public ClinicaResponseDTO criarClinica(ClinicaRequestDTO dto) {
        // Verificar se o CNPJ já existe
        Optional<Organizacao> organizacaoExistente = organizacaoRepository.findByCnpj(dto.cnpj());
        if (organizacaoExistente.isPresent()) {
            throw new RegraNegocioException("Já existe uma organização cadastrada com o CNPJ informado");
        }

        // Criar a clínica
        Clinica clinica = new Clinica(dto.nome(), dto.cnpj(), dto.tipo());

        // Salvar no banco de dados
        Clinica clinicaSalva = (Clinica) organizacaoRepository.save(clinica);

        // Mapear para DTO de resposta
        return new ClinicaResponseDTO(
                clinicaSalva.getId(),
                clinicaSalva.getNome(),
                clinicaSalva.getCnpj(),
                clinicaSalva.getTipo()
        );
    }
}