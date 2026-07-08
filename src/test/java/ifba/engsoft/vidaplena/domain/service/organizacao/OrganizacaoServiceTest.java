package ifba.engsoft.vidaplena.domain.service.organizacao;

import ifba.engsoft.vidaplena.domain.dto.organizacao.ClinicaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.ClinicaResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.EmpresaRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.organizacao.EmpresaResponseDTO;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.organizacao.Clinica;
import ifba.engsoft.vidaplena.domain.model.organizacao.Empresa;
import ifba.engsoft.vidaplena.domain.model.familia.VinculoDependencia;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.organizacao.OrganizacaoRepository;
import ifba.engsoft.vidaplena.domain.repository.familia.VinculoDependenciaRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.ProfissionalRepository;
import ifba.engsoft.vidaplena.domain.service.familia.RegraNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganizacaoServiceTest {

    @Mock
    private OrganizacaoRepository organizacaoRepository;

    @Mock
    private VinculoDependenciaRepository vinculoDependenciaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @InjectMocks
    private OrganizacaoService organizacaoService;

    private Empresa empresa;
    private Clinica clinica;
    private EmpresaRequestDTO empresaRequestDTO;
    private ClinicaRequestDTO clinicaRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        empresa = new Empresa("Empresa Teste", "12345678000199", "Tecnologia");
        empresa.setId(UUID.randomUUID());

        clinica = new Clinica("Clinica Teste", "98765432000188", "Medicina Geral");
        clinica.setId(UUID.randomUUID());

        empresaRequestDTO = new EmpresaRequestDTO("Empresa Teste", "12345678000199", "Tecnologia");
        clinicaRequestDTO = new ClinicaRequestDTO("Clinica Teste", "98765432000188", "Medicina Geral");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveCriarEmpresaComSucesso() {
        when(organizacaoRepository.findByCnpj(empresaRequestDTO.cnpj())).thenReturn(Optional.empty());
        when(organizacaoRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaResponseDTO response = organizacaoService.criarEmpresa(empresaRequestDTO);

        assertNotNull(response);
        assertEquals(empresa.getNome(), response.nome());
        verify(organizacaoRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    void deveLancarExceptionAoCriarEmpresaComCnpjJaExistente() {
        when(organizacaoRepository.findByCnpj(empresaRequestDTO.cnpj())).thenReturn(Optional.of(empresa));

        assertThrows(RegraNegocioException.class, () -> organizacaoService.criarEmpresa(empresaRequestDTO));
        verify(organizacaoRepository, never()).save(any());
    }

    @Test
    void deveCriarClinicaComSucesso() {
        when(organizacaoRepository.findByCnpj(clinicaRequestDTO.cnpj())).thenReturn(Optional.empty());
        when(organizacaoRepository.save(any(Clinica.class))).thenReturn(clinica);

        ClinicaResponseDTO response = organizacaoService.criarClinica(clinicaRequestDTO);

        assertNotNull(response);
        assertEquals(clinica.getNome(), response.nome());
        verify(organizacaoRepository, times(1)).save(any(Clinica.class));
    }

    @Test
    void deveLancarExceptionAoCriarClinicaComCnpjJaExistente() {
        when(organizacaoRepository.findByCnpj(clinicaRequestDTO.cnpj())).thenReturn(Optional.of(clinica));

        assertThrows(RegraNegocioException.class, () -> organizacaoService.criarClinica(clinicaRequestDTO));
        verify(organizacaoRepository, never()).save(any());
    }

    @Test
    void deveExcluirOrganizacaoComSucesso() {
        when(organizacaoRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
        when(vinculoDependenciaRepository.countActiveVinculosByEmpresaId(empresa.getId())).thenReturn(0L);
        when(usuarioRepository.countByEmpresaId(empresa.getId())).thenReturn(0L);

        organizacaoService.excluirOrganizacao(empresa.getId());

        verify(organizacaoRepository, times(1)).delete(empresa);
    }

    @Test
    void deveLancarExceptionAoExcluirOrganizacaoComVinculosAtivos() {
        when(organizacaoRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
        when(vinculoDependenciaRepository.countActiveVinculosByEmpresaId(empresa.getId())).thenReturn(3L);

        assertThrows(RegraNegocioException.class, () -> organizacaoService.excluirOrganizacao(empresa.getId()));
        verify(organizacaoRepository, never()).delete(any());
    }

    @Test
    void deveLancarExceptionAoExcluirOrganizacaoComMembrosVinculados() {
        when(organizacaoRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
        when(vinculoDependenciaRepository.countActiveVinculosByEmpresaId(empresa.getId())).thenReturn(0L);
        when(usuarioRepository.countByEmpresaId(empresa.getId())).thenReturn(5L);

        assertThrows(RegraNegocioException.class, () -> organizacaoService.excluirOrganizacao(empresa.getId()));
        verify(organizacaoRepository, never()).delete(any());
    }

    @Test
    void deveInativarOrganizacaoComSucesso() {
        when(organizacaoRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
        when(vinculoDependenciaRepository.findActiveVinculosByEmpresaId(empresa.getId())).thenReturn(List.of(new VinculoDependencia()));
        
        Usuario u = new Usuario("Membro", "11111111111", "membro@example.com", "senha", "71999999999", null, StatusUsuario.ATIVO, Set.of(TipoUsuario.PACIENTE));
        when(usuarioRepository.findByEmpresaId(empresa.getId())).thenReturn(List.of(u));

        organizacaoService.inativarOrganizacao(empresa.getId());

        verify(vinculoDependenciaRepository, times(1)).save(any(VinculoDependencia.class));
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(organizacaoRepository, times(1)).save(empresa);
        assertTrue(empresa.getNome().startsWith("[INATIVO]"));
    }

    @Test
    void deveBuscarClinicaPorIdComSucessoParaAdmin() {
        mockAuthentication("admin@example.com");
        Usuario admin = new Usuario("Admin", "11111111111", "admin@example.com", "senha", "71999999999", null, StatusUsuario.ATIVO, Set.of(TipoUsuario.ADMINISTRADOR));
        when(usuarioRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(organizacaoRepository.findById(clinica.getId())).thenReturn(Optional.of(clinica));

        ClinicaResponseDTO response = organizacaoService.buscarClinicaPorId(clinica.getId());

        assertNotNull(response);
        assertEquals(clinica.getNome(), response.nome());
    }

    @Test
    void deveLancarExceptionAoBuscarClinicaInexistente() {
        UUID idInvalido = UUID.randomUUID();
        when(organizacaoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> organizacaoService.buscarClinicaPorId(idInvalido));
    }

    @Test
    void deveLancarExceptionAoBuscarClinicaComTipoIncorreto() {
        when(organizacaoRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));

        assertThrows(RegraNegocioException.class, () -> organizacaoService.buscarClinicaPorId(empresa.getId()));
    }

    @Test
    void deveLancarExceptionAoBuscarClinicaParaRepresentanteDeOutraClinica() {
        mockAuthentication("rep@example.com");
        Clinica outraClinica = new Clinica("Outra Clinica", "11111111111111", "Tipo");
        outraClinica.setId(UUID.randomUUID());

        Usuario rep = new Usuario("Rep", "22222222222", "rep@example.com", "senha", "71999999999", null, StatusUsuario.ATIVO, Set.of(TipoUsuario.REPRESENTANTE_EMPRESA));
        rep.setClinica(outraClinica);

        when(usuarioRepository.findByEmailIgnoreCase("rep@example.com")).thenReturn(Optional.of(rep));
        when(organizacaoRepository.findById(clinica.getId())).thenReturn(Optional.of(clinica));

        assertThrows(AccessDeniedException.class, () -> organizacaoService.buscarClinicaPorId(clinica.getId()));
    }

    @Test
    void deveBuscarEmpresaPorIdComSucessoParaAdmin() {
        mockAuthentication("admin@example.com");
        Usuario admin = new Usuario("Admin", "11111111111", "admin@example.com", "senha", "71999999999", null, StatusUsuario.ATIVO, Set.of(TipoUsuario.ADMINISTRADOR));
        when(usuarioRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(organizacaoRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));

        EmpresaResponseDTO response = organizacaoService.buscarEmpresaPorId(empresa.getId());

        assertNotNull(response);
        assertEquals(empresa.getNome(), response.nome());
    }

    @Test
    void deveLancarExceptionAoBuscarEmpresaInexistente() {
        UUID idInvalido = UUID.randomUUID();
        when(organizacaoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> organizacaoService.buscarEmpresaPorId(idInvalido));
    }

    @Test
    void deveLancarExceptionAoBuscarEmpresaComTipoIncorreto() {
        when(organizacaoRepository.findById(clinica.getId())).thenReturn(Optional.of(clinica));

        assertThrows(RegraNegocioException.class, () -> organizacaoService.buscarEmpresaPorId(clinica.getId()));
    }

    @Test
    void deveLancarExceptionAoBuscarEmpresaParaRepresentanteDeOutraEmpresa() {
        mockAuthentication("rep@example.com");
        Empresa outraEmpresa = new Empresa("Outra Empresa", "22222222222222", "Setor");
        outraEmpresa.setId(UUID.randomUUID());

        Usuario rep = new Usuario("Rep", "22222222222", "rep@example.com", "senha", "71999999999", null, StatusUsuario.ATIVO, Set.of(TipoUsuario.REPRESENTANTE_EMPRESA));
        rep.setEmpresa(outraEmpresa);

        when(usuarioRepository.findByEmailIgnoreCase("rep@example.com")).thenReturn(Optional.of(rep));
        when(organizacaoRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));

        assertThrows(AccessDeniedException.class, () -> organizacaoService.buscarEmpresaPorId(empresa.getId()));
    }

    @Test
    void deveListarClinicas() {
        when(organizacaoRepository.findAll()).thenReturn(List.of(clinica, empresa));

        List<ClinicaResponseDTO> list = organizacaoService.listarClinicas();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(clinica.getId(), list.get(0).id());
    }

    @Test
    void deveListarEmpresas() {
        when(organizacaoRepository.findAll()).thenReturn(List.of(clinica, empresa));

        List<EmpresaResponseDTO> list = organizacaoService.listarEmpresas();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(empresa.getId(), list.get(0).id());
    }

    private void mockAuthentication(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }
}
