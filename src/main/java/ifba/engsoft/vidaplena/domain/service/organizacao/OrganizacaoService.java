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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class OrganizacaoService {

    @Autowired
    private OrganizacaoRepository organizacaoRepository;
    /**
     * Cria uma nova empresa com o CNPJ e setor informados.
     * Valida se o CNPJ já não está em uso por outra organização.
     * 
     * @param dto DTO contendo os dados da empresa a ser criada
     * @return DTO de resposta com os dados da empresa criada
     * @throws RegraNegocioException se o CNPJ já estiver em uso
     */
    @Transactional
    public EmpresaResponseDTO criarEmpresa(EmpresaRequestDTO dto) {
        // Verificar se o CNPJ já existe
        Optional<Organizacao> organizacaoExistente = organizacaoRepository.findByCnpj(dto.cnpj());
        if (organizacaoExistente.isPresent()) {
            throw new RegraNegocioException(
                    "Já existe uma organização cadastrada com o CNPJ informado: " + dto.cnpj()
            );
        }

        // Criar a empresa
        Empresa empresa = new Empresa(dto.nome(), dto.cnpj(), dto.setor());

        Empresa empresaSalva = (Empresa) organizacaoRepository.save(empresa);

        return new EmpresaResponseDTO(
                empresaSalva.getId(),
                empresaSalva.getNome(),
                empresaSalva.getCnpj(),
                empresaSalva.getSetor()
        );
    }

    /**
     * Cria uma nova clínica com o CNPJ e tipo informados.
     * Valida se o CNPJ já não está em uso por outra organização.
     * 
     * @param dto DTO contendo os dados da clínica a ser criada
     * @return DTO de resposta com os dados da clínica criada
     * @throws RegraNegocioException se o CNPJ já estiver em uso
     */
    @Transactional
    public ClinicaResponseDTO criarClinica(ClinicaRequestDTO dto) {
        // Verificar se o CNPJ já existe
        Optional<Organizacao> organizacaoExistente = organizacaoRepository.findByCnpj(dto.cnpj());
        if (organizacaoExistente.isPresent()) {
            throw new RegraNegocioException(
                    "Já existe uma organização cadastrada com o CNPJ informado: " + dto.cnpj()
            );
        }

        // Criar a clínica
        Clinica clinica = new Clinica(dto.nome(), dto.cnpj(), dto.tipo());

        Clinica clinicaSalva = (Clinica) organizacaoRepository.save(clinica);

        return new ClinicaResponseDTO(
                clinicaSalva.getId(),
                clinicaSalva.getNome(),
                clinicaSalva.getCnpj(),
                clinicaSalva.getTipo()
        );
    }

    /**
     * REGRA DE EXCLUSÃO DE ORGANIZAÇÕES
     * 
     * Impedir a exclusão física de uma Organização (Empresa, Clínica ou qualquer
     * subtipo) que possua membros, funcionários ou dependentes vinculados ativos.
     * 
     * Esta regra garante:
     * 1. Não há perda de dados históricos de vínculo
     * 2. Não há integridade referencial quebrada
     * 3. A auditoria pode rastrear todas as relações passadas
     * 4. Organizações devem ser inativadas logicamente antes da exclusão
     * 
     * @param organizacaoId ID da organização a ser excluída
     * @throws RegraNegocioException se a organização não existir ou se houver vínculos ativos
     */
    @Transactional
    public void excluirOrganizacao(UUID organizacaoId) {
        // Verificar se a organização existe
        Organizacao organizacao = organizacaoRepository.findById(organizacaoId)
                .orElseThrow(() -> new RegraNegocioException(
                        "Organização com ID " + organizacaoId + " não encontrada."
                ));

        // Verificar se existem vínculos de dependência ativos envolvendo esta organização
        // Como Organizacao é um MappedSuperclass, precisamos verificar pelas subclasses
        long vinculosAtivosEmpresa = 0;
        long vinculosAtivosClinica = 0;

        if (organizacao instanceof Empresa) {
            vinculosAtivosEmpresa = contarVinculosAtivosPorOrganizacaoId(organizacaoId);
        } else if (organizacao instanceof Clinica) {
            vinculosAtivosClinica = contarVinculosAtivosPorOrganizacaoId(organizacaoId);
        }

        long totalVinculosAtivos = vinculosAtivosEmpresa + vinculosAtivosClinica;

        if (totalVinculosAtivos > 0) {
            throw new RegraNegocioException(
                    "Não é possível excluir a organização '" + organizacao.getNome() + 
                    "' (ID: " + organizacaoId + ", tipo: " + organizacao.getClass().getSimpleName() + 
                    "). Existem " + totalVinculosAtivos + " vínculo(s) ativo(s) envolvendo esta organização. " +
                    "Por segurança, organizações com vínculos ativos devem ser inativadas logicamente antes da exclusão."
            );
        }

        // Verificar se a organização possui membros/funcionários vinculados
        int membrosVinculados = contarMembrosVinculados(organizacaoId);

        if (membrosVinculados > 0) {
            throw new RegraNegocioException(
                    "Não é possível excluir a organização '" + organizacao.getNome() + 
                    "' (ID: " + organizacaoId + "). Ela possui " + membrosVinculados + 
                    " membro(s)/funcionário(s) vinculado(s). Remova todos os vínculos antes de excluir."
            );
        }

        // Todas as validações passaram, proceder com a exclusão 
        organizacaoRepository.delete(organizacao);
    }

    /**
     * Soft delete (inativação) de uma organização. Marca a organização como inativa, inativa todos os vínculos ativos e remove membros vinculados.
     * 
     * @param organizacaoId ID da organização a ser inativada
     * @return DTO de resposta com os dados da organização inativada
     * @throws RegraNegocioException se a organização não existir
     */
    @Transactional
    public OrganizacaoResponseDTO inativarOrganizacao(UUID organizacaoId) {
        Organizacao organizacao = organizacaoRepository.findById(organizacaoId)
                .orElseThrow(() -> new RegraNegocioException(
                        "Organização com ID " + organizacaoId + " não encontrada."
                ));

        // Inativar todos os vínculos de dependência associados a esta organização
        inativarVinculosDaOrganizacao(organizacaoId);

        // Remover membros/funcionários vinculados
        removerMembrosVinculados(organizacaoId);

        // Renomear para indicar inativação
        organizacao.setNome("[INATIVO] " + organizacao.getNome());

        organizacaoRepository.save(organizacao);

        return new OrganizacaoResponseDTO(
                organizacao.getId(),
                organizacao.getNome(),
                organizacao.getCnpj(),
                organizacao.getClass().getSimpleName()
        );
    }

    /**
     * Conta o número de vínculos de dependência ativos envolvendo uma organização.
     */
    private long contarVinculosAtivosPorOrganizacaoId(UUID organizacaoId) {
        // TODO: Implementar a lógica para contar vínculos de dependência ativos envolvendo a organização.
        return 0; 
    }

    /**
     * Conta o número de membros/funcionários vinculados a uma organização.
     */
    private int contarMembrosVinculados(UUID organizacaoId) {
        // TODO: Implementar a lógica para contar membros/funcionários vinculados à organização.
        return 0;
    }

    /**
     * Inativa todos os vínculos de dependência associados a uma organização.
     */
    private void inativarVinculosDaOrganizacao(UUID organizacaoId) {
        // TODO: Implementar a lógica para inativar todos os vínculos de dependência associados à organização.
    }

    /**
     * Remove todos os membros vinculados a uma organização.
     */
    private void removerMembrosVinculados(UUID organizacaoId) {
        // TODO: Implementar a lógica para remover membros vinculados à organização.
    }

    /**
     * Record auxiliar para resposta genérica de organizações.
     */
    private record OrganizacaoResponseDTO(
            UUID id,
            String nome,
            String cnpj,
            String tipo
    ) {
    }
}
