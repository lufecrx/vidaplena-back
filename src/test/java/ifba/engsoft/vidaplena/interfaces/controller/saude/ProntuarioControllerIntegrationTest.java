package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.ProntuarioDTO;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.model.saude.Prontuario;
import ifba.engsoft.vidaplena.domain.service.saude.ProntuarioService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProntuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProntuarioService prontuarioService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private static final String BASE_URL = "/api/prontuarios";

    @Test
    @DisplayName("Deve negar acesso sem autenticação (HTTP 401)")
    void deveNegarAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve negar acesso (HTTP 403) a perfil PACIENTE")
    void deveNegarAcessoParaPaciente() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve permitir acesso e criar prontuário para perfil MEDICO (HTTP 201)")
    void devePermitirCriarProntuarioParaMedico() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        ProntuarioDTO dto = new ProntuarioDTO(null, pacienteId, "Observações iniciais");
        
        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        Prontuario prontuario = new Prontuario(paciente);
        prontuario.setId(UUID.randomUUID());
        prontuario.setObservacoesGerais("Observações iniciais");

        when(prontuarioService.criarProntuario(any(Prontuario.class))).thenReturn(prontuario);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pacienteId").value(pacienteId.toString()))
                .andExpect(jsonPath("$.observacoesGerais").value("Observações iniciais"));

        verify(prontuarioService, times(1)).criarProntuario(any(Prontuario.class));
    }

    @Test
    @WithMockUser(roles = "NUTRICIONISTA")
    @DisplayName("Deve obter prontuário por ID para perfil NUTRICIONISTA (HTTP 200)")
    void deveObterProntuarioParaNutricionista() throws Exception {
        UUID id = UUID.randomUUID();
        Paciente paciente = new Paciente();
        paciente.setId(UUID.randomUUID());
        Prontuario prontuario = new Prontuario(paciente);
        prontuario.setId(id);
        prontuario.setObservacoesGerais("Dieta balanceada");

        when(prontuarioService.obterProntuarioPorId(id)).thenReturn(prontuario);

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.observacoesGerais").value("Dieta balanceada"));

        verify(prontuarioService, times(1)).obterProntuarioPorId(id);
    }

    @Test
    @WithMockUser(roles = "PERSONAL_TRAINER")
    @DisplayName("Deve obter prontuário por paciente ID para perfil PERSONAL_TRAINER (HTTP 200)")
    void deveObterProntuarioPorPacienteParaPersonalTrainer() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        Paciente paciente = new Paciente();
        paciente.setId(pacienteId);
        Prontuario prontuario = new Prontuario(paciente);
        prontuario.setId(UUID.randomUUID());
        prontuario.setObservacoesGerais("Foco em hipertrofia");

        when(prontuarioService.obterProntuarioPorPacienteId(pacienteId)).thenReturn(prontuario);

        mockMvc.perform(get(BASE_URL + "/paciente/{pacienteId}", pacienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(pacienteId.toString()))
                .andExpect(jsonPath("$.observacoesGerais").value("Foco em hipertrofia"));

        verify(prontuarioService, times(1)).obterProntuarioPorPacienteId(pacienteId);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Deve obter histórico clínico para perfil ADMINISTRADOR (HTTP 200)")
    void deveObterHistoricoClinicoParaAdministrador() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        when(prontuarioService.obterHistoricoClinicoPaciente(pacienteId)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/paciente/{pacienteId}/historico", pacienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(prontuarioService, times(1)).obterHistoricoClinicoPaciente(pacienteId);
    }
}
