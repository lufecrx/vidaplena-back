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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
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
}
