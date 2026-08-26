package ifba.engsoft.vidaplena.infrastructure.seeder;

import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.familia.Familia;
import ifba.engsoft.vidaplena.domain.model.familia.VinculoDependencia;
import ifba.engsoft.vidaplena.domain.model.organizacao.Clinica;
import ifba.engsoft.vidaplena.domain.model.organizacao.Empresa;
import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import ifba.engsoft.vidaplena.domain.model.saude.Prontuario;
import ifba.engsoft.vidaplena.domain.model.saude.RegistroAtendimento;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.FamiliaRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.VinculoDependenciaRepository;
import ifba.engsoft.vidaplena.domain.repository.organizacao.OrganizacaoRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.AgendamentoRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.NotaRetificacaoRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.PacienteRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.ProfissionalRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.ProntuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.RegistroAtendimentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataSeederTest {

    @Autowired
    private DataSeeder dataSeeder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private FamiliaRepository familiaRepository;

    @Autowired
    private VinculoDependenciaRepository vinculoDependenciaRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ProntuarioRepository prontuarioRepository;

    @Autowired
    private RegistroAtendimentoRepository registroAtendimentoRepository;

    @Autowired
    private NotaRetificacaoRepository notaRetificacaoRepository;

    @Autowired
    private ifba.engsoft.vidaplena.domain.repository.saude.DocumentoProntuarioRepository documentoProntuarioRepository;

    @Test
    @DisplayName("Deve popular contas para todos os perfis com credenciais válidas")
    void devePopularContasParaTodosOsPerfis() {
        // 1. Validar perfis e autenticação com senhas planas
        validarUsuario("admin@vidaplena.com", "Admin@123456", TipoUsuario.ADMINISTRADOR);
        validarUsuario("medico@vidaplena.com", "Medico@123456", TipoUsuario.MEDICO);
        validarUsuario("nutri@vidaplena.com", "Nutri@123456", TipoUsuario.NUTRICIONISTA);
        validarUsuario("personal@vidaplena.com", "Personal@123456", TipoUsuario.PERSONAL_TRAINER);
        validarUsuario("admin.clinica@vidaplena.com", "Admin@123456", TipoUsuario.FUNCIONARIO_ADMINISTRATIVO);
        validarUsuario("empresa@vidaplena.com", "Empresa@123456", TipoUsuario.REPRESENTANTE_EMPRESA);
        validarUsuario("responsavel@vidaplena.com", "Resp@123456", TipoUsuario.RESPONSAVEL);
        validarUsuario("paciente@vidaplena.com", "Paciente@123456", TipoUsuario.PACIENTE);
        validarUsuario("dependente@vidaplena.com", "Dep@123456", TipoUsuario.PACIENTE);
        validarUsuario("cuidador@vidaplena.com", "Cuidador@123456", TipoUsuario.CUIDADOR);

        // 2. Validar organizações criadas
        assertThat(organizacaoRepository.findByCnpj("11222333000181")).isPresent();
        assertThat(organizacaoRepository.findByCnpj("44555666000199")).isPresent();

        // 3. Validar entidades de profissionais
        Usuario medicoUser = usuarioRepository.findByEmailIgnoreCase("medico@vidaplena.com").orElseThrow();
        Optional<Profissional> profissional = profissionalRepository.findByUsuario(medicoUser);
        assertThat(profissional).isPresent();
        assertThat(profissional.get().getRegistroConselho()).isEqualTo("CRM/BA 12345");

        // 4. Validar vínculo de família e dependência
        Usuario responsavel = usuarioRepository.findByEmailIgnoreCase("responsavel@vidaplena.com").orElseThrow();
        Usuario dependente = usuarioRepository.findByEmailIgnoreCase("dependente@vidaplena.com").orElseThrow();
        VinculoDependencia vinculo = vinculoDependenciaRepository.findByResponsavelAndDependenteAndDataFimIsNull(responsavel, dependente);
        assertThat(vinculo).isNotNull();

        Optional<Familia> familia = familiaRepository.findByNome("Família Costa Silva");
        assertThat(familia).isPresent();
        assertThat(familia.get().getMembros()).extracting(Usuario::getEmail)
                .contains(responsavel.getEmail(), dependente.getEmail());

        // 5. Validar fluxo clínico: Prontuário, Agendamentos, Atendimento e Retificação
        Usuario pacienteUser = usuarioRepository.findByEmailIgnoreCase("paciente@vidaplena.com").orElseThrow();
        Paciente paciente = pacienteRepository.findByUsuario(pacienteUser).orElseThrow();
        Prontuario prontuario = prontuarioRepository.findByPacienteId(paciente.getId());
        assertThat(prontuario).isNotNull();

        List<Agendamento> agendamentos = agendamentoRepository.findByPacienteId(paciente.getId());
        assertThat(agendamentos).isNotEmpty();

        Agendamento agendamento = agendamentos.getFirst();
        Optional<RegistroAtendimento> registro = registroAtendimentoRepository.findByAgendamentoId(agendamento.getId());
        assertThat(registro).isPresent();
        assertThat(registro.get().isFinalizado()).isTrue();
        assertThat(registro.get().getDiagnostico()).contains("Hipertensão");

        assertThat(notaRetificacaoRepository.findAllByRegistroAtendimentoIdOrderByDataRegistroAsc(registro.get().getId()))
                .isNotEmpty();

        assertThat(documentoProntuarioRepository.findAllByProntuarioIdOrderByDataCriacaoDesc(prontuario.getId()))
                .hasSize(2);
    }

    @Test
    @DisplayName("Deve garantir idempotência ao executar DataSeeder múltiplas vezes")
    void deveGarantirIdempotencia() {
        long contagemUsuariosAntes = usuarioRepository.count();
        long contagemOrganizacoesAntes = organizacaoRepository.count();
        long contagemPacientesAntes = pacienteRepository.count();
        long contagemProfissionaisAntes = profissionalRepository.count();
        long contagemDocumentosAntes = documentoProntuarioRepository.count();

        // Executar o seeder uma segunda vez
        assertDoesNotThrow(() -> dataSeeder.run());

        // Validar que não houve duplicação de dados
        assertThat(usuarioRepository.count()).isEqualTo(contagemUsuariosAntes);
        assertThat(organizacaoRepository.count()).isEqualTo(contagemOrganizacoesAntes);
        assertThat(pacienteRepository.count()).isEqualTo(contagemPacientesAntes);
        assertThat(profissionalRepository.count()).isEqualTo(contagemProfissionaisAntes);
        assertThat(documentoProntuarioRepository.count()).isEqualTo(contagemDocumentosAntes);
    }

    private void validarUsuario(String email, String senhaPlana, TipoUsuario tipoEsperado) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(email);
        assertThat(usuarioOpt).isPresent();
        Usuario usuario = usuarioOpt.get();
        assertThat(usuario.getStatus()).isEqualTo(StatusUsuario.ATIVO);
        assertThat(usuario.possuiTipo(tipoEsperado)).isTrue();
        assertThat(passwordEncoder.matches(senhaPlana, usuario.getSenha()))
                .as("Senha cadastrada para %s deve ser compatível com BCrypt", email)
                .isTrue();
    }
}
