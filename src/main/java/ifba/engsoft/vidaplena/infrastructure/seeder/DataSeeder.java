package ifba.engsoft.vidaplena.infrastructure.seeder;

import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.familia.Familia;
import ifba.engsoft.vidaplena.domain.model.familia.TipoDependencia;
import ifba.engsoft.vidaplena.domain.model.familia.VinculoDependencia;
import ifba.engsoft.vidaplena.domain.model.organizacao.Clinica;
import ifba.engsoft.vidaplena.domain.model.organizacao.Empresa;
import ifba.engsoft.vidaplena.domain.model.organizacao.Organizacao;
import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.Especialidade;
import ifba.engsoft.vidaplena.domain.model.saude.NotaRetificacao;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.model.saude.Profissional;
import ifba.engsoft.vidaplena.domain.model.saude.Prontuario;
import ifba.engsoft.vidaplena.domain.model.saude.RegistroAtendimento;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import ifba.engsoft.vidaplena.domain.model.saude.TipoAtendimento;
import ifba.engsoft.vidaplena.domain.model.saude.TipoSanguineo;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Componente idempotente responsável por inicializar e popular o banco de dados
 * com contas de teste para todos os perfis do sistema VidaPlena, organizações parceiras,
 * prontuários, agendamentos e registros clínicos.
 *
 * Ativo exclusivamente fora do ambiente de produção (@Profile("!prod")).
 */
@Component
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizacaoRepository organizacaoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final FamiliaRepository familiaRepository;
    private final VinculoDependenciaRepository vinculoDependenciaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ProntuarioRepository prontuarioRepository;
    private final RegistroAtendimentoRepository registroAtendimentoRepository;
    private final NotaRetificacaoRepository notaRetificacaoRepository;

    public DataSeeder(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            OrganizacaoRepository organizacaoRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            FamiliaRepository familiaRepository,
            VinculoDependenciaRepository vinculoDependenciaRepository,
            AgendamentoRepository agendamentoRepository,
            ProntuarioRepository prontuarioRepository,
            RegistroAtendimentoRepository registroAtendimentoRepository,
            NotaRetificacaoRepository notaRetificacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizacaoRepository = organizacaoRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.familiaRepository = familiaRepository;
        this.vinculoDependenciaRepository = vinculoDependenciaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.prontuarioRepository = prontuarioRepository;
        this.registroAtendimentoRepository = registroAtendimentoRepository;
        this.notaRetificacaoRepository = notaRetificacaoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Iniciando DataSeeder / DatabaseInitializer para ambiente de desenvolvimento/testes...");

        // 1. Organizações (Clínica e Empresa parceira)
        Clinica clinica = seedClinica();
        Empresa empresa = seedEmpresa();

        // 2. Usuários e Perfis (RBAC completo)
        Usuario admin = seedUsuario(
                "Administrador do Sistema",
                "10000000001",
                "admin@vidaplena.com",
                "Admin@123456",
                "71981110001",
                LocalDate.of(1985, 1, 10),
                Set.of(TipoUsuario.ADMINISTRADOR),
                null,
                null
        );

        Usuario medicoUser = seedUsuario(
                "Dr. Carlos Eduardo Menezes",
                "10000000002",
                "medico@vidaplena.com",
                "Medico@123456",
                "71981110002",
                LocalDate.of(1980, 4, 15),
                Set.of(TipoUsuario.MEDICO),
                clinica,
                null
        );
        Profissional medico = seedProfissional(medicoUser, "CRM/BA 12345", Especialidade.CLINICO_GERAL, clinica.getId());

        Usuario nutriUser = seedUsuario(
                "Dra. Juliana Ferreira Lima",
                "10000000003",
                "nutri@vidaplena.com",
                "Nutri@123456",
                "71981110003",
                LocalDate.of(1990, 7, 22),
                Set.of(TipoUsuario.NUTRICIONISTA),
                clinica,
                null
        );
        seedProfissional(nutriUser, "CRN-5 67890", Especialidade.NUTRICAO, clinica.getId());

        Usuario personalUser = seedUsuario(
                "Prof. Roberto Albuquerque",
                "10000000004",
                "personal@vidaplena.com",
                "Personal@123456",
                "71981110004",
                LocalDate.of(1988, 11, 5),
                Set.of(TipoUsuario.PERSONAL_TRAINER),
                clinica,
                null
        );
        seedProfissional(personalUser, "CREF 012345-G/BA", Especialidade.FISIOTERAPIA, clinica.getId());

        seedUsuario(
                "Fernanda Soares Recepção",
                "10000000005",
                "admin.clinica@vidaplena.com",
                "Admin@123456",
                "71981110005",
                LocalDate.of(1993, 9, 18),
                Set.of(TipoUsuario.FUNCIONARIO_ADMINISTRATIVO),
                clinica,
                null
        );

        seedUsuario(
                "Lucas Silveira RH",
                "10000000006",
                "empresa@vidaplena.com",
                "Empresa@123456",
                "71981110006",
                LocalDate.of(1987, 3, 30),
                Set.of(TipoUsuario.REPRESENTANTE_EMPRESA),
                null,
                empresa
        );

        Usuario responsavelUser = seedUsuario(
                "Mariana Costa Silva",
                "10000000007",
                "responsavel@vidaplena.com",
                "Resp@123456",
                "71981110007",
                LocalDate.of(1982, 12, 14),
                Set.of(TipoUsuario.RESPONSAVEL, TipoUsuario.PACIENTE),
                null,
                null
        );
        seedPaciente(responsavelUser, TipoSanguineo.B_POSITIVO, List.of(), List.of(), "Sem histórico relevante.");

        Usuario pacienteUser = seedUsuario(
                "Ana Beatriz Santos",
                "10000000008",
                "paciente@vidaplena.com",
                "Paciente@123456",
                "71981110008",
                LocalDate.of(1995, 6, 25),
                Set.of(TipoUsuario.PACIENTE),
                null,
                null
        );
        Paciente paciente = seedPaciente(
                pacienteUser,
                TipoSanguineo.O_POSITIVO,
                List.of("Dipirona", "Penicilina"),
                List.of("Losartana 50mg", "Sinvastatina 20mg"),
                "Histórico familiar paterno de hipertensão arterial sistêmica."
        );

        Usuario dependenteUser = seedUsuario(
                "Pedro Silva (Dependente)",
                "10000000009",
                "dependente@vidaplena.com",
                "Dep@123456",
                "71981110009",
                LocalDate.of(2015, 8, 10),
                Set.of(TipoUsuario.PACIENTE),
                null,
                null
        );
        Paciente dependentePaciente = seedPaciente(
                dependenteUser,
                TipoSanguineo.A_POSITIVO,
                List.of("Pólen", "Ácaros"),
                List.of(),
                "Rinite alérgica sazonal."
        );

        seedUsuario(
                "Cláudia Pereira Cuidadora",
                "10000000010",
                "cuidador@vidaplena.com",
                "Cuidador@123456",
                "71981110010",
                LocalDate.of(1986, 5, 12),
                Set.of(TipoUsuario.CUIDADOR),
                null,
                null
        );

        // 3. Vínculo Familiar e de Dependência
        seedVinculoDependencia(responsavelUser, dependenteUser, TipoDependencia.CRIANCA);
        seedFamilia("Família Costa Silva", List.of(responsavelUser, dependenteUser));

        // 4. Fluxo Clínico: Prontuários, Agendamentos, Atendimento e Retificação
        seedFluxoClinico(paciente, dependentePaciente, medico, clinica);

        // 5. Exibir tabela resumo formatada no log de inicialização
        imprimirTabelaResumo();
    }

    private Clinica seedClinica() {
        String cnpj = "11222333000181";
        Optional<Organizacao> orgExistente = organizacaoRepository.findByCnpj(cnpj);
        if (orgExistente.isPresent() && orgExistente.get() instanceof Clinica clinica) {
            return clinica;
        }

        Clinica clinica = new Clinica("Clínica Vida Plena Saúde Integrada", cnpj, "Clínica Multidisciplinar");
        return (Clinica) organizacaoRepository.save(clinica);
    }

    private Empresa seedEmpresa() {
        String cnpj = "44555666000199";
        Optional<Organizacao> orgExistente = organizacaoRepository.findByCnpj(cnpj);
        if (orgExistente.isPresent() && orgExistente.get() instanceof Empresa empresa) {
            return empresa;
        }

        Empresa empresa = new Empresa("TechCorp Soluções Tecnológicas", cnpj, "Tecnologia da Informação");
        return (Empresa) organizacaoRepository.save(empresa);
    }

    private Usuario seedUsuario(
            String nome,
            String cpf,
            String email,
            String senhaPlana,
            String telefone,
            LocalDate dataNascimento,
            Set<TipoUsuario> tipos,
            Clinica clinica,
            Empresa empresa) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(email);
        if (usuarioOpt.isPresent()) {
            Usuario existente = usuarioOpt.get();
            boolean alterado = false;
            if (clinica != null && existente.getClinica() == null) {
                existente.setClinica(clinica);
                alterado = true;
            }
            if (empresa != null && existente.getEmpresa() == null) {
                existente.setEmpresa(empresa);
                alterado = true;
            }
            return alterado ? usuarioRepository.save(existente) : existente;
        }

        Usuario usuario = new Usuario(
                nome,
                cpf,
                email.toLowerCase().trim(),
                passwordEncoder.encode(senhaPlana),
                telefone,
                dataNascimento,
                StatusUsuario.ATIVO,
                tipos
        );

        if (clinica != null) {
            usuario.setClinica(clinica);
        }
        if (empresa != null) {
            usuario.setEmpresa(empresa);
        }

        return usuarioRepository.save(usuario);
    }

    private Profissional seedProfissional(Usuario usuario, String registroConselho, Especialidade especialidade, java.util.UUID clinicaId) {
        Optional<Profissional> profissionalOpt = profissionalRepository.findByUsuario(usuario);
        if (profissionalOpt.isPresent()) {
            return profissionalOpt.get();
        }

        Profissional profissional = new Profissional(usuario);
        profissional.setRegistroConselho(registroConselho);
        profissional.setEspecialidade(especialidade);
        profissional.setClinicaId(clinicaId);
        return profissionalRepository.save(profissional);
    }

    private Paciente seedPaciente(
            Usuario usuario,
            TipoSanguineo tipoSanguineo,
            List<String> alergias,
            List<String> medicamentosContinuos,
            String historicoFamiliar) {

        Optional<Paciente> pacienteOpt = pacienteRepository.findByUsuario(usuario);
        if (pacienteOpt.isPresent()) {
            return pacienteOpt.get();
        }

        Paciente paciente = new Paciente(usuario);
        paciente.setTipoSanguineo(tipoSanguineo);
        paciente.setAlergias(new ArrayList<>(alergias));
        paciente.setMedicamentosContinuos(new ArrayList<>(medicamentosContinuos));
        paciente.setHistoricoFamiliar(historicoFamiliar);
        return pacienteRepository.save(paciente);
    }

    private void seedVinculoDependencia(Usuario responsavel, Usuario dependente, TipoDependencia tipo) {
        VinculoDependencia vinculoExistente = vinculoDependenciaRepository
                .findByResponsavelAndDependenteAndDataFimIsNull(responsavel, dependente);

        if (vinculoExistente == null) {
            VinculoDependencia vinculo = new VinculoDependencia(
                    tipo,
                    LocalDate.now().minusYears(1),
                    responsavel,
                    dependente
            );
            vinculoDependenciaRepository.save(vinculo);
        }
    }

    private void seedFamilia(String nome, List<Usuario> membros) {
        Optional<Familia> familiaOpt = familiaRepository.findByNome(nome);
        if (familiaOpt.isEmpty()) {
            Familia familia = new Familia(nome);
            familia.setMembros(new ArrayList<>(membros));
            familiaRepository.save(familia);
        }
    }

    private void seedFluxoClinico(Paciente paciente, Paciente dependente, Profissional medico, Clinica clinica) {
        // Prontuário do Paciente Titular
        Prontuario prontuario = prontuarioRepository.findByPacienteId(paciente.getId());
        if (prontuario == null) {
            prontuario = new Prontuario(
                    paciente,
                    "Paciente em acompanhamento cardiológico de rotina. Nega tabagismo e etilismo."
            );
            prontuario = prontuarioRepository.save(prontuario);
        }

        // Prontuário do Dependente
        Prontuario prontuarioDep = prontuarioRepository.findByPacienteId(dependente.getId());
        if (prontuarioDep == null) {
            prontuarioDep = new Prontuario(
                    dependente,
                    "Acompanhamento do desenvolvimento infantil e controle de alergias respiratórias."
            );
            prontuarioRepository.save(prontuarioDep);
        }

        // Agendamento 1 (Concluído no passado)
        LocalDateTime dataHoraPassada = LocalDateTime.now().minusDays(3).withHour(9).withMinute(0).withSecond(0).withNano(0);
        Agendamento agendamentoConcluido;
        if (!agendamentoRepository.existsByPacienteIdAndProfissionalIdAndDataHora(paciente.getId(), medico.getId(), dataHoraPassada)) {
            agendamentoConcluido = new Agendamento(
                    paciente,
                    medico,
                    clinica,
                    null,
                    dataHoraPassada,
                    StatusAgendamento.CONCLUIDO,
                    TipoAtendimento.PRESENCIAL,
                    "Check-up cardiológico anual e investigação de cefaleia tensional."
            );
            agendamentoConcluido.setDataHoraFim(dataHoraPassada.plusHours(1));
            agendamentoConcluido = agendamentoRepository.save(agendamentoConcluido);
        } else {
            agendamentoConcluido = agendamentoRepository.findByPacienteId(paciente.getId()).stream()
                    .filter(a -> a.getDataHora().equals(dataHoraPassada))
                    .findFirst()
                    .orElse(null);
        }

        // Registro de Atendimento Clínico para a consulta concluída
        if (agendamentoConcluido != null && !registroAtendimentoRepository.existsByAgendamentoId(agendamentoConcluido.getId())) {
            RegistroAtendimento registro = new RegistroAtendimento(prontuario, medico, agendamentoConcluido);
            registro.setDataRegistro(dataHoraPassada.plusMinutes(45));
            registro.setSintomasRelatados("Paciente relata cansaço aos esforços e episódios esporádicos de cefaleia matinal.");
            registro.setDiagnostico("Hipertensão arterial sistêmica estágio 1 (CID-10 I10).");
            registro.setPrescricaoMedica("1. Losartana Potássica 50mg - Tomar 1 comprimido pela manhã em jejum.\n2. Retorno em 60 dias.");
            registro.setPrescricaoEnfermagem("Aferição de pressão arterial 2x ao dia com registro em diário de PA.");
            registro.setNotasClinicas("Realizado ECG em repouso com traçado normal. Solicitados exames laboratoriais de perfil lipídico e glicemia.");
            registro.setFinalizado(true);

            RegistroAtendimento registroSalvo = registroAtendimentoRepository.save(registro);

            // Nota de Retificação Clínica
            NotaRetificacao nota = new NotaRetificacao(
                    registroSalvo,
                    medico,
                    "Retificação: Caso o paciente apresente tosse seca persistente ou tontura, antecipar o retorno para readequação posológica."
            );
            nota.setDataRegistro(dataHoraPassada.plusDays(1).withHour(14).withMinute(20));
            notaRetificacaoRepository.save(nota);
        }

        // Agendamento 2 (Futuro / Confirmado)
        LocalDateTime dataHoraFutura = LocalDateTime.now().plusDays(5).withHour(14).withMinute(0).withSecond(0).withNano(0);
        if (!agendamentoRepository.existsByPacienteIdAndProfissionalIdAndDataHora(paciente.getId(), medico.getId(), dataHoraFutura)) {
            Agendamento agendamentoFuturo = new Agendamento(
                    paciente,
                    medico,
                    clinica,
                    null,
                    dataHoraFutura,
                    StatusAgendamento.CONFIRMADO,
                    TipoAtendimento.TELECONSULTA,
                    "Retorno e reavaliação dos exames laboratoriais e mapa de pressão arterial."
            );
            agendamentoFuturo.setDataHoraFim(dataHoraFutura.plusHours(1));
            agendamentoRepository.save(agendamentoFuturo);
        }
    }

    private void imprimirTabelaResumo() {
        String resumo = """
                
                ====================================================================================================================================================
                                                                VIDAPLENA - SEED DE DADOS DE TESTE INICIALIZADO COM SUCESSO                                         
                ====================================================================================================================================================
                | Perfil (Role)                | Nome                           | E-mail                            | Senha        | Detalhes / Vínculo            |
                +------------------------------+--------------------------------+-----------------------------------+--------------+-------------------------------+
                | ADMINISTRADOR                | Administrador do Sistema       | admin@vidaplena.com               | Admin@123456 | Acesso Total (Root / Admin)   |
                | MEDICO                       | Dr. Carlos Eduardo Menezes     | medico@vidaplena.com              | Medico@123456| CRM/BA 12345 (Clínico Geral)  |
                | NUTRICIONISTA                | Dra. Juliana Ferreira Lima     | nutri@vidaplena.com               | Nutri@123456 | CRN-5 67890 (Nutrição)        |
                | PERSONAL_TRAINER             | Prof. Roberto Albuquerque      | personal@vidaplena.com            | Personal@123456| CREF 012345-G/BA (Fisioterapia)|
                | FUNCIONARIO_ADMINISTRATIVO   | Fernanda Soares Recepção       | admin.clinica@vidaplena.com       | Admin@123456 | Recepção da Clínica VidaPlena |
                | REPRESENTANTE_EMPRESA        | Lucas Silveira RH              | empresa@vidaplena.com             | Empresa@123456| TechCorp (Empresa Parceira)   |
                | RESPONSAVEL                  | Mariana Costa Silva            | responsavel@vidaplena.com         | Resp@123456  | Responsável Família Silva     |
                | PACIENTE                     | Ana Beatriz Santos             | paciente@vidaplena.com            | Paciente@123456| Paciente com Prontuário/Agend |
                | PACIENTE (Dependente)        | Pedro Silva (Dependente)       | dependente@vidaplena.com          | Dep@123456   | Filho de Mariana Costa Silva  |
                | CUIDADOR                     | Cláudia Pereira Cuidadora      | cuidador@vidaplena.com            | Cuidador@123456| Cuidadora Domiciliar          |
                ====================================================================================================================================================
                | ORGANIZAÇÕES CRIADAS:                                                                                                                            |
                |  * Clínica: Clínica Vida Plena Saúde Integrada (CNPJ: 11.222.333/0001-81)                                                                        |
                |  * Empresa: TechCorp Soluções Tecnológicas (CNPJ: 44.555.666/0001-99)                                                                            |
                | FLUXO CLÍNICO GERADO:                                                                                                                            |
                |  * 2 Prontuários (Ana Beatriz e Pedro Silva)                                                                                                    |
                |  * 1 Agendamento CONCLUIDO com Registro de Atendimento finalizado e Nota de Retificação                                                         |
                |  * 1 Agendamento CONFIRMADO futuro para teleconsulta de retorno                                                                                  |
                |  * 1 Vínculo de Dependência ativo (Mãe/Filho) e 1 Registro de Família                                                                            |
                ====================================================================================================================================================""";

        log.info(resumo);
    }
}
