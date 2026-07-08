package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.PacienteDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.PacienteResponseDTO;
import ifba.engsoft.vidaplena.domain.model.saude.TipoSanguineo;
import ifba.engsoft.vidaplena.domain.service.saude.PacienteService;
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
class PacienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PacienteService pacienteService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private static final String BASE_URL = "/api/pacientes";

    @Test
    @DisplayName("Deve negar acesso a endpoint de paciente sem autenticação (HTTP 401)")
    void deveNegarAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve permitir criar paciente com autenticação (HTTP 201)")
    void devePermitirCriarPacienteAutenticado() throws Exception {
        UUID id = UUID.randomUUID();
        PacienteDTO dto = new PacienteDTO(
                UUID.randomUUID().toString(),
                TipoSanguineo.O_POSITIVO,
                List.of("Nenhuma"),
                List.of("Nenhum"),
                "Nenhum"
        );
        PacienteResponseDTO response = new PacienteResponseDTO(
                id,
                dto.usuarioId(),
                dto.tipoSanguineo(),
                dto.alergias(),
                dto.medicamentosContinuos(),
                dto.historicoFamiliar()
        );

        when(pacienteService.criarPaciente(any(PacienteDTO.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.tipoSanguineo").value("O_POSITIVO"));

        verify(pacienteService, times(1)).criarPaciente(any(PacienteDTO.class));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve permitir atualizar paciente com autenticação (HTTP 200)")
    void devePermitirAtualizarPacienteAutenticado() throws Exception {
        UUID id = UUID.randomUUID();
        PacienteDTO dto = new PacienteDTO(
                UUID.randomUUID().toString(),
                TipoSanguineo.O_POSITIVO,
                List.of("Poeira"),
                List.of("Nenhum"),
                "Diabetes"
        );
        PacienteResponseDTO response = new PacienteResponseDTO(
                id,
                dto.usuarioId(),
                dto.tipoSanguineo(),
                dto.alergias(),
                dto.medicamentosContinuos(),
                dto.historicoFamiliar()
        );

        when(pacienteService.atualizarPaciente(eq(id), any(PacienteDTO.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alergias[0]").value("Poeira"));

        verify(pacienteService, times(1)).atualizarPaciente(eq(id), any(PacienteDTO.class));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve obter paciente por ID (HTTP 200)")
    void deveObterPacientePorId() throws Exception {
        UUID id = UUID.randomUUID();
        PacienteResponseDTO response = new PacienteResponseDTO(
                id,
                UUID.randomUUID().toString(),
                TipoSanguineo.AB_NEGATIVO,
                List.of("Polen"),
                List.of("Aspirina"),
                "Cardiaco"
        );

        when(pacienteService.obterPaciente(id)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.tipoSanguineo").value("AB_NEGATIVO"));

        verify(pacienteService, times(1)).obterPaciente(id);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve listar todos os pacientes (HTTP 200)")
    void deveListarPacientes() throws Exception {
        UUID id = UUID.randomUUID();
        PacienteResponseDTO response = new PacienteResponseDTO(
                id,
                UUID.randomUUID().toString(),
                TipoSanguineo.B_POSITIVO,
                List.of("Nenhuma"),
                List.of("Nenhum"),
                "Nenhum"
        );

        when(pacienteService.listarPacientes()).thenReturn(List.of(response));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()));

        verify(pacienteService, times(1)).listarPacientes();
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve deletar paciente por ID (HTTP 204)")
    void deveDeletarPaciente() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(pacienteService).deletarPaciente(id);

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNoContent());

        verify(pacienteService, times(1)).deletarPaciente(id);
    }
}
