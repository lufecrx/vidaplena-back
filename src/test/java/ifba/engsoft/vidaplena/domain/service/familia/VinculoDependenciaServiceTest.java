package ifba.engsoft.vidaplena.domain.service.familia;

import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaResponseDTO;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.familia.TipoDependencia;
import ifba.engsoft.vidaplena.domain.model.familia.VinculoDependencia;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.VinculoDependenciaRepository;
import ifba.engsoft.vidaplena.domain.dto.familia.CadastroDependenteRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.DependenteResponseDTO;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.model.saude.TipoSanguineo;
import ifba.engsoft.vidaplena.domain.repository.saude.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VinculoDependenciaServiceTest {

    @Mock
    private VinculoDependenciaRepository vinculoDependenciaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private VinculoDependenciaService vinculoDependenciaService;

    private Usuario responsavel;
    private Usuario dependente;
    private VinculoDependencia vinculo;
    private VinculoDependenciaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UUID responsavelId = UUID.randomUUID();
        responsavel = new Usuario(
                "Responsavel",
                "11122233344",
                "responsavel@example.com",
                "senha",
                "71999999999",
                LocalDate.now().minusYears(25),
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.RESPONSAVEL)
        );
        ReflectionTestUtils.setField(responsavel, "id", responsavelId);

        UUID dependenteId = UUID.randomUUID();
        dependente = new Usuario(
                "Dependente",
                "55566677788",
                "dependente@example.com",
                "senha",
                "71999999999",
                LocalDate.now().minusYears(5),
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)
        );
        ReflectionTestUtils.setField(dependente, "id", dependenteId);

        vinculo = new VinculoDependencia();
        vinculo.setId(UUID.randomUUID());
        vinculo.setResponsavel(responsavel);
        vinculo.setDependente(dependente);
        vinculo.setTipo(TipoDependencia.CRIANCA);
        vinculo.setDataInicio(LocalDate.now());

        requestDTO = new VinculoDependenciaRequestDTO(
                responsavelId,
                dependenteId,
                TipoDependencia.CRIANCA,
                LocalDate.now(),
                null
        );
    }

    @Test
    void deveCriarVinculoComSucesso() {
        when(usuarioRepository.findById(responsavel.getId())).thenReturn(Optional.of(responsavel));
        when(usuarioRepository.findById(dependente.getId())).thenReturn(Optional.of(dependente));
        when(vinculoDependenciaRepository.findByResponsavelIdAndDependenteIdAndDataFimIsNull(responsavel.getId(), dependente.getId()))
                .thenReturn(null);
        when(vinculoDependenciaRepository.save(any(VinculoDependencia.class))).thenReturn(vinculo);

        VinculoDependenciaResponseDTO response = vinculoDependenciaService.criarVinculo(requestDTO);

        assertNotNull(response);
        assertEquals(responsavel.getNome(), response.responsavelNome());
        assertEquals(dependente.getNome(), response.dependenteNome());
        verify(vinculoDependenciaRepository, times(1)).save(any(VinculoDependencia.class));
    }

    @Test
    void deveLancarExceptionAoCriarVinculoConsigoMesmo() {
        VinculoDependenciaRequestDTO autoRequest = new VinculoDependenciaRequestDTO(
                responsavel.getId(),
                responsavel.getId(),
                TipoDependencia.CRIANCA,
                LocalDate.now(),
                null
        );

        assertThrows(RegraNegocioException.class, () -> vinculoDependenciaService.criarVinculo(autoRequest));
        verify(vinculoDependenciaRepository, never()).save(any());
    }

    @Test
    void deveLancarExceptionAoCriarVinculoComDataInicioFutura() {
        VinculoDependenciaRequestDTO futureRequest = new VinculoDependenciaRequestDTO(
                responsavel.getId(),
                dependente.getId(),
                TipoDependencia.CRIANCA,
                LocalDate.now().plusDays(1),
                null
        );

        assertThrows(RegraNegocioException.class, () -> vinculoDependenciaService.criarVinculo(futureRequest));
        verify(vinculoDependenciaRepository, never()).save(any());
    }

    @Test
    void deveLancarExceptionAoCriarVinculoComResponsavelSemDataNascimento() {
        responsavel.setDataNascimento(null);
        when(usuarioRepository.findById(responsavel.getId())).thenReturn(Optional.of(responsavel));

        assertThrows(RegraNegocioException.class, () -> vinculoDependenciaService.criarVinculo(requestDTO));
        verify(vinculoDependenciaRepository, never()).save(any());
    }

    @Test
    void deveLancarExceptionAoCriarVinculoComResponsavelMenorDeIdade() {
        responsavel.setDataNascimento(LocalDate.now().minusYears(17));
        when(usuarioRepository.findById(responsavel.getId())).thenReturn(Optional.of(responsavel));

        assertThrows(RegraNegocioException.class, () -> vinculoDependenciaService.criarVinculo(requestDTO));
        verify(vinculoDependenciaRepository, never()).save(any());
    }

    @Test
    void deveLancarExceptionAoCriarVinculoJaExistenteEAtivo() {
        when(usuarioRepository.findById(responsavel.getId())).thenReturn(Optional.of(responsavel));
        when(usuarioRepository.findById(dependente.getId())).thenReturn(Optional.of(dependente));
        when(vinculoDependenciaRepository.findByResponsavelIdAndDependenteIdAndDataFimIsNull(responsavel.getId(), dependente.getId()))
                .thenReturn(vinculo);

        assertThrows(RegraNegocioException.class, () -> vinculoDependenciaService.criarVinculo(requestDTO));
        verify(vinculoDependenciaRepository, never()).save(any());
    }

    @Test
    void deveInativarVinculoComSucesso() {
        when(vinculoDependenciaRepository.findById(vinculo.getId())).thenReturn(Optional.of(vinculo));
        when(vinculoDependenciaRepository.save(any(VinculoDependencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VinculoDependenciaResponseDTO response = vinculoDependenciaService.inativarVinculo(vinculo.getId());

        assertNotNull(response);
        assertEquals(LocalDate.now(), response.dataFim());
        verify(vinculoDependenciaRepository, times(1)).save(vinculo);
    }

    @Test
    void deveLancarExceptionAoInativarVinculoJaInativo() {
        vinculo.setDataFim(LocalDate.now().minusDays(1));
        when(vinculoDependenciaRepository.findById(vinculo.getId())).thenReturn(Optional.of(vinculo));

        assertThrows(RegraNegocioException.class, () -> vinculoDependenciaService.inativarVinculo(vinculo.getId()));
        verify(vinculoDependenciaRepository, never()).save(any());
    }

    @Test
    void deveCadastrarDependenteCriancaComSucessoEGerarEmailPadrao() {
        when(usuarioRepository.findByEmailIgnoreCase(responsavel.getEmail())).thenReturn(Optional.of(responsavel));
        when(usuarioRepository.findByCpf("99988877766")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-pwd");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            if (u.getId() == null) {
                ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
            }
            return u;
        });

        when(pacienteRepository.findByUsuario(any(Usuario.class))).thenReturn(Optional.empty());
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> {
            Paciente p = invocation.getArgument(0);
            if (p.getId() == null) {
                ReflectionTestUtils.setField(p, "id", UUID.randomUUID());
            }
            return p;
        });

        when(vinculoDependenciaRepository.findByResponsavelIdAndDependenteIdAndDataFimIsNull(any(), any()))
                .thenReturn(null);
        when(vinculoDependenciaRepository.save(any(VinculoDependencia.class))).thenAnswer(invocation -> {
            VinculoDependencia v = invocation.getArgument(0);
            if (v.getId() == null) {
                ReflectionTestUtils.setField(v, "id", UUID.randomUUID());
            }
            return v;
        });

        CadastroDependenteRequestDTO dto = new CadastroDependenteRequestDTO(
                null,
                "Lucas Silva",
                "999.888.777-66",
                LocalDate.now().minusYears(8),
                TipoDependencia.CRIANCA,
                null, // Sem email fornecido: deve gerar automaticamente
                "71988887777",
                TipoSanguineo.O_POSITIVO,
                List.of("Dipirona"),
                List.of(),
                "Histórico de asma infantil",
                LocalDate.now(),
                null
        );

        DependenteResponseDTO response = vinculoDependenciaService.cadastrarDependente(dto, responsavel.getEmail());

        assertNotNull(response);
        assertEquals("Lucas Silva", response.nome());
        assertEquals("99988877766", response.cpf());
        assertTrue(response.email().contains("99988877766"));
        assertEquals(8, response.idade());
        assertEquals(TipoDependencia.CRIANCA, response.tipo());
        assertEquals(TipoSanguineo.O_POSITIVO, response.tipoSanguineo());
        assertEquals(List.of("Dipirona"), response.alergias());
        assertEquals(responsavel.getId(), response.responsavelId());

        verify(usuarioRepository, atLeastOnce()).save(any(Usuario.class));
        verify(pacienteRepository, times(1)).save(any(Paciente.class));
        verify(vinculoDependenciaRepository, times(1)).save(any(VinculoDependencia.class));
    }

    @Test
    void deveCadastrarDependenteIdosoComSucesso() {
        when(usuarioRepository.findByEmailIgnoreCase(responsavel.getEmail())).thenReturn(Optional.of(responsavel));
        when(usuarioRepository.findByCpf("11133355577")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByEmailIgnoreCase("antonio.silva@email.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-pwd");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            if (u.getId() == null) {
                ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
            }
            return u;
        });
        when(pacienteRepository.findByUsuario(any(Usuario.class))).thenReturn(Optional.empty());
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> {
            Paciente p = invocation.getArgument(0);
            if (p.getId() == null) {
                ReflectionTestUtils.setField(p, "id", UUID.randomUUID());
            }
            return p;
        });
        when(vinculoDependenciaRepository.save(any(VinculoDependencia.class))).thenAnswer(invocation -> {
            VinculoDependencia v = invocation.getArgument(0);
            if (v.getId() == null) {
                ReflectionTestUtils.setField(v, "id", UUID.randomUUID());
            }
            return v;
        });

        CadastroDependenteRequestDTO dto = new CadastroDependenteRequestDTO(
                null,
                "Antônio Silva",
                "11133355577",
                LocalDate.now().minusYears(72),
                TipoDependencia.IDOSO,
                "antonio.silva@email.com",
                "71999990000",
                TipoSanguineo.A_POSITIVO,
                List.of(),
                List.of("Losartana 50mg"),
                "Hipertensão crônica",
                LocalDate.now(),
                null
        );

        DependenteResponseDTO response = vinculoDependenciaService.cadastrarDependente(dto, responsavel.getEmail());

        assertNotNull(response);
        assertEquals("Antônio Silva", response.nome());
        assertEquals(72, response.idade());
        assertEquals(TipoDependencia.IDOSO, response.tipo());
        assertEquals("antonio.silva@email.com", response.email());
        assertEquals(List.of("Losartana 50mg"), response.medicamentosContinuos());
    }

    @Test
    void deveLancarExceptionAoCadastrarDependenteComAutoDependenciaPorCpf() {
        when(usuarioRepository.findByEmailIgnoreCase(responsavel.getEmail())).thenReturn(Optional.of(responsavel));

        CadastroDependenteRequestDTO dto = new CadastroDependenteRequestDTO(
                null,
                "Mesmo Nome",
                responsavel.getCpf(), // Mesmo CPF do responsável
                LocalDate.now().minusYears(10),
                TipoDependencia.CRIANCA,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(RegraNegocioException.class, () ->
                vinculoDependenciaService.cadastrarDependente(dto, responsavel.getEmail()));
    }

    @Test
    void deveLancarExceptionAoCadastrarCriancaCom18AnosOuMais() {
        when(usuarioRepository.findByEmailIgnoreCase(responsavel.getEmail())).thenReturn(Optional.of(responsavel));

        CadastroDependenteRequestDTO dto = new CadastroDependenteRequestDTO(
                null,
                "Jovem Maior",
                "12345678909",
                LocalDate.now().minusYears(18), // Idade 18 anos
                TipoDependencia.CRIANCA,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(RegraNegocioException.class, () ->
                vinculoDependenciaService.cadastrarDependente(dto, responsavel.getEmail()));
    }

    @Test
    void deveLancarExceptionAoCadastrarIdosoComMenosDe60Anos() {
        when(usuarioRepository.findByEmailIgnoreCase(responsavel.getEmail())).thenReturn(Optional.of(responsavel));

        CadastroDependenteRequestDTO dto = new CadastroDependenteRequestDTO(
                null,
                "Adulto Meia Idade",
                "12345678909",
                LocalDate.now().minusYears(59), // Menor que 60 anos
                TipoDependencia.IDOSO,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(RegraNegocioException.class, () ->
                vinculoDependenciaService.cadastrarDependente(dto, responsavel.getEmail()));
    }

    @Test
    void deveLancarExceptionQuandoUsuarioNaoAdminTentaCadastrarParaOutroResponsavel() {
        when(usuarioRepository.findByEmailIgnoreCase(responsavel.getEmail())).thenReturn(Optional.of(responsavel));

        UUID outroResponsavelId = UUID.randomUUID();
        CadastroDependenteRequestDTO dto = new CadastroDependenteRequestDTO(
                outroResponsavelId,
                "Dependente Alheio",
                "12345678909",
                LocalDate.now().minusYears(10),
                TipoDependencia.CRIANCA,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(RegraNegocioException.class, () ->
                vinculoDependenciaService.cadastrarDependente(dto, responsavel.getEmail()));
    }

    @Test
    void deveListarDependentesDoResponsavelComSucesso() {
        when(usuarioRepository.findByEmailIgnoreCase(responsavel.getEmail())).thenReturn(Optional.of(responsavel));
        when(vinculoDependenciaRepository.findByResponsavelIdAndDataFimIsNull(responsavel.getId()))
                .thenReturn(List.of(vinculo));

        Paciente p = new Paciente(dependente);
        p.setTipoSanguineo(TipoSanguineo.AB_POSITIVO);
        when(pacienteRepository.findByUsuario(dependente)).thenReturn(Optional.of(p));

        List<DependenteResponseDTO> lista = vinculoDependenciaService.listarDependentes(responsavel.getEmail(), null, true);

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(dependente.getNome(), lista.get(0).nome());
        assertEquals(TipoSanguineo.AB_POSITIVO, lista.get(0).tipoSanguineo());
    }
}
