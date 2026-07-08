package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.ProfissionalDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.ProfissionalResponseDTO;
import ifba.engsoft.vidaplena.domain.model.saude.Especialidade;
import ifba.engsoft.vidaplena.domain.service.saude.ProfissionalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
class ProfissionalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfissionalService profissionalService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private static final String BASE_URL = "/api/profissionais";

    @Test
    @DisplayName("Deve negar acesso a endpoint de profissional sem autenticação (HTTP 401)")
    void deveNegarAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve permitir criar profissional com autenticação (HTTP 201)")
    void devePermitirCriarProfissionalAutenticado() throws Exception {
        UUID id = UUID.randomUUID();
        ProfissionalDTO dto = new ProfissionalDTO(
                UUID.randomUUID().toString(),
                "CRM/BA 12345",
                Especialidade.CLINICO_GERAL,
                UUID.randomUUID()
        );
        ProfissionalResponseDTO response = new ProfissionalResponseDTO(
                id,
                dto.usuarioId(),
                dto.registroConselho(),
                dto.especialidade(),
                dto.clinicaId()
        );

        when(profissionalService.criarProfissional(any(ProfissionalDTO.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.registroConselho").value("CRM/BA 12345"));

        verify(profissionalService, times(1)).criarProfissional(any(ProfissionalDTO.class));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve permitir atualizar profissional com autenticação (HTTP 200)")
    void devePermitirAtualizarProfissionalAutenticado() throws Exception {
        UUID id = UUID.randomUUID();
        ProfissionalDTO dto = new ProfissionalDTO(
                UUID.randomUUID().toString(),
                "CRM/BA 54321",
                Especialidade.NUTRICAO,
                UUID.randomUUID()
        );
        ProfissionalResponseDTO response = new ProfissionalResponseDTO(
                id,
                dto.usuarioId(),
                dto.registroConselho(),
                dto.especialidade(),
                dto.clinicaId()
        );

        when(profissionalService.atualizarProfissional(eq(id), any(ProfissionalDTO.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registroConselho").value("CRM/BA 54321"));

        verify(profissionalService, times(1)).atualizarProfissional(eq(id), any(ProfissionalDTO.class));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve obter profissional por ID (HTTP 200)")
    void deveObterProfissionalPorId() throws Exception {
        UUID id = UUID.randomUUID();
        ProfissionalResponseDTO response = new ProfissionalResponseDTO(
                id,
                UUID.randomUUID().toString(),
                "CRM/BA 99999",
                Especialidade.CLINICO_GERAL,
                UUID.randomUUID()
        );

        when(profissionalService.obterProfissional(id)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.especialidade").value("CLINICO_GERAL"));

        verify(profissionalService, times(1)).obterProfissional(id);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve listar todos os profissionais (HTTP 200)")
    void deveListarProfissionais() throws Exception {
        UUID id = UUID.randomUUID();
        ProfissionalResponseDTO response = new ProfissionalResponseDTO(
                id,
                UUID.randomUUID().toString(),
                "CRM/BA 88888",
                Especialidade.NUTRICAO,
                UUID.randomUUID()
        );

        when(profissionalService.listarProfissionais()).thenReturn(List.of(response));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()));

        verify(profissionalService, times(1)).listarProfissionais();
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve deletar profissional por ID (HTTP 204)")
    void deveDeletarProfissional() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(profissionalService).deletarProfissional(id);

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNoContent());

        verify(profissionalService, times(1)).deletarProfissional(id);
    }
}
