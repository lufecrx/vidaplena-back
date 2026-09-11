package ifba.engsoft.vidaplena.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifba.engsoft.vidaplena.domain.dto.familia.CadastroDependenteRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.DependenteResponseDTO;
import ifba.engsoft.vidaplena.domain.dto.familia.VinculoDependenciaResponseDTO;
import ifba.engsoft.vidaplena.domain.model.familia.TipoDependencia;
import ifba.engsoft.vidaplena.domain.model.saude.TipoSanguineo;
import ifba.engsoft.vidaplena.domain.service.familia.VinculoDependenciaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DependenteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VinculoDependenciaService vinculoDependenciaService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private static final String BASE_URL = "/api/v1/dependentes";
    private static final String VINCULOS_DEPENDENTES_URL = "/api/v1/vinculos/dependentes";

    @Test
    @DisplayName("Deve negar acesso ao cadastro de dependente sem token de autenticação (HTTP 401)")
    void deveNegarAcessoSemAutenticacao() throws Exception {
        CadastroDependenteRequestDTO dto = new CadastroDependenteRequestDTO(
                null,
                "Lucas Silva",
                "12345678901",
                LocalDate.now().minusYears(5),
                TipoDependencia.CRIANCA,
                null,
                null,
                TipoSanguineo.O_POSITIVO,
                List.of(),
                List.of(),
                null,
                null,
                null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "responsavel@vidaplena.com", roles = "RESPONSAVEL")
    @DisplayName("Deve cadastrar dependente com sucesso pelo endpoint /api/v1/dependentes (HTTP 201)")
    void deveCadastrarDependenteComSucessoViaEndpointDedicado() throws Exception {
        UUID vinculoId = UUID.randomUUID();
        UUID dependenteId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();

        CadastroDependenteRequestDTO dto = new CadastroDependenteRequestDTO(
                null,
                "Lucas Silva Santos",
                "123.456.789-01",
                LocalDate.now().minusYears(7),
                TipoDependencia.CRIANCA,
                null,
                "71988887777",
                TipoSanguineo.O_POSITIVO,
                List.of("Penicilina"),
                List.of(),
                "Histórico de asma",
                LocalDate.now(),
                null
        );

        DependenteResponseDTO responseDTO = new DependenteResponseDTO(
                vinculoId,
                dependenteId,
                pacienteId,
                responsavelId,
                "Mariana Costa Silva",
                "Lucas Silva Santos",
                "12345678901",
                "dep.12345678901@dependente.vidaplena.local",
                "71988887777",
                dto.dataNascimento(),
                7,
                TipoDependencia.CRIANCA,
                LocalDate.now(),
                null,
                TipoSanguineo.O_POSITIVO,
                List.of("Penicilina"),
                List.of(),
                "Histórico de asma"
        );

        when(vinculoDependenciaService.cadastrarDependente(any(), eq("responsavel@vidaplena.com")))
                .thenReturn(responseDTO);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vinculoId").value(vinculoId.toString()))
                .andExpect(jsonPath("$.nome").value("Lucas Silva Santos"))
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.tipo").value("CRIANCA"))
                .andExpect(jsonPath("$.idade").value(7))
                .andExpect(jsonPath("$.responsavelNome").value("Mariana Costa Silva"))
                .andExpect(jsonPath("$.tipoSanguineo").value("O_POSITIVO"))
                .andExpect(jsonPath("$.alergias[0]").value("Penicilina"));

        verify(vinculoDependenciaService, times(1))
                .cadastrarDependente(any(), eq("responsavel@vidaplena.com"));
    }

    @Test
    @WithMockUser(username = "responsavel@vidaplena.com", roles = "RESPONSAVEL")
    @DisplayName("Deve cadastrar dependente com sucesso pelo endpoint /api/v1/vinculos/dependentes (HTTP 201)")
    void deveCadastrarDependenteComSucessoViaEndpointVinculos() throws Exception {
        UUID vinculoId = UUID.randomUUID();
        UUID dependenteId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();

        CadastroDependenteRequestDTO dto = new CadastroDependenteRequestDTO(
                null,
                "Lucas Silva Santos",
                "12345678901",
                LocalDate.now().minusYears(7),
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

        DependenteResponseDTO responseDTO = new DependenteResponseDTO(
                vinculoId,
                dependenteId,
                pacienteId,
                responsavelId,
                "Mariana Costa Silva",
                "Lucas Silva Santos",
                "12345678901",
                "dep.12345678901@dependente.vidaplena.local",
                null,
                dto.dataNascimento(),
                7,
                TipoDependencia.CRIANCA,
                LocalDate.now(),
                null,
                null,
                List.of(),
                List.of(),
                null
        );

        when(vinculoDependenciaService.cadastrarDependente(any(), eq("responsavel@vidaplena.com")))
                .thenReturn(responseDTO);

        mockMvc.perform(post(VINCULOS_DEPENDENTES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Lucas Silva Santos"));

        verify(vinculoDependenciaService, times(1))
                .cadastrarDependente(any(), eq("responsavel@vidaplena.com"));
    }

    @Test
    @WithMockUser(username = "responsavel@vidaplena.com", roles = "RESPONSAVEL")
    @DisplayName("Deve listar dependentes do usuário logado (HTTP 200)")
    void deveListarDependentesDoUsuarioLogado() throws Exception {
        DependenteResponseDTO dep = new DependenteResponseDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Mariana Costa Silva",
                "Lucas Silva Santos",
                "12345678901",
                "dep.12345678901@dependente.vidaplena.local",
                null,
                LocalDate.now().minusYears(7),
                7,
                TipoDependencia.CRIANCA,
                LocalDate.now(),
                null,
                TipoSanguineo.O_POSITIVO,
                List.of(),
                List.of(),
                null
        );

        when(vinculoDependenciaService.listarDependentes(eq("responsavel@vidaplena.com"), isNull(), eq(true)))
                .thenReturn(List.of(dep));

        mockMvc.perform(get(BASE_URL).param("apenasAtivos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Lucas Silva Santos"))
                .andExpect(jsonPath("$[0].tipo").value("CRIANCA"));

        verify(vinculoDependenciaService, times(1))
                .listarDependentes(eq("responsavel@vidaplena.com"), isNull(), eq(true));
    }

    @Test
    @WithMockUser(username = "responsavel@vidaplena.com", roles = "RESPONSAVEL")
    @DisplayName("Deve obter dependente por vinculoId com sucesso (HTTP 200)")
    void deveObterDependentePorVinculoId() throws Exception {
        UUID vinculoId = UUID.randomUUID();
        DependenteResponseDTO dep = new DependenteResponseDTO(
                vinculoId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Mariana Costa Silva",
                "Pedro Silva",
                "10000000009",
                "dep.10000000009@dependente.vidaplena.local",
                null,
                LocalDate.of(2015, 8, 10),
                10,
                TipoDependencia.CRIANCA,
                LocalDate.now(),
                null,
                TipoSanguineo.A_POSITIVO,
                List.of("Pólen"),
                List.of(),
                "Rinite alérgica"
        );

        when(vinculoDependenciaService.obterDependente(vinculoId, "responsavel@vidaplena.com"))
                .thenReturn(dep);

        mockMvc.perform(get(BASE_URL + "/" + vinculoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vinculoId").value(vinculoId.toString()))
                .andExpect(jsonPath("$.nome").value("Pedro Silva"))
                .andExpect(jsonPath("$.tipoSanguineo").value("A_POSITIVO"));
    }

    @Test
    @WithMockUser(username = "responsavel@vidaplena.com", roles = "RESPONSAVEL")
    @DisplayName("Deve inativar vínculo com dependente com sucesso (HTTP 204)")
    void deveInativarDependenteComSucesso() throws Exception {
        UUID vinculoId = UUID.randomUUID();

        VinculoDependenciaResponseDTO inativado = new VinculoDependenciaResponseDTO(
                vinculoId,
                "Mariana Costa Silva",
                "Lucas Silva Santos",
                TipoDependencia.CRIANCA,
                LocalDate.now().minusYears(1),
                LocalDate.now()
        );
        when(vinculoDependenciaService.inativarVinculo(vinculoId)).thenReturn(inativado);

        mockMvc.perform(delete(BASE_URL + "/" + vinculoId))
                .andExpect(status().isNoContent());

        verify(vinculoDependenciaService, times(1)).inativarVinculo(vinculoId);
    }

    @Test
    @WithMockUser(username = "responsavel@vidaplena.com", roles = "RESPONSAVEL")
    @DisplayName("Deve rejeitar requisição com dados obrigatórios ausentes (HTTP 400)")
    void deveRejeitarRequisicaoComDadosInvalidos() throws Exception {
        // Objeto com campos em branco e tipo nulo
        CadastroDependenteRequestDTO dtoInvalido = new CadastroDependenteRequestDTO(
                null,
                "", // nome vazio
                "", // cpf vazio
                null, // nascimento nulo
                null, // tipo nulo
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest());

        verify(vinculoDependenciaService, never()).cadastrarDependente(any(), any());
    }
}
