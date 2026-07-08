package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.AgendamentoRequestDTO;
import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import ifba.engsoft.vidaplena.domain.service.saude.AgendamentoService;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendamentoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendamentoService agendamentoService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private static final String BASE_URL = "/api/agendamentos";

    @Test
    @DisplayName("Deve negar acesso a endpoint de agendamento sem autenticação (HTTP 401)")
    void deveNegarAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get(BASE_URL + "/profissional/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve criar agendamento com sucesso (HTTP 201)")
    void deveCriarAgendamentoComSucesso() throws Exception {
        AgendamentoRequestDTO request = new AgendamentoRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                "Consulta de Rotina"
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andExpect(jsonPath("$.motivoConsulta").value("Consulta de Rotina"));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve confirmar agendamento com sucesso (HTTP 200)")
    void deveConfirmarAgendamentoComSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        Agendamento agendamento = new Agendamento();
        agendamento.setId(id);
        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        agendamento.setDataHoraInicio(LocalDateTime.now().plusDays(1));
        agendamento.setDataHoraFim(LocalDateTime.now().plusDays(1).plusHours(1));

        when(agendamentoService.atualizarStatus(eq(id), eq(StatusAgendamento.CONFIRMADO)))
                .thenReturn(agendamento);

        mockMvc.perform(put(BASE_URL + "/{id}/confirmar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));

        verify(agendamentoService, times(1)).atualizarStatus(eq(id), eq(StatusAgendamento.CONFIRMADO));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve cancelar agendamento com sucesso (HTTP 200)")
    void deveCancelarAgendamentoComSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        Agendamento agendamento = new Agendamento();
        agendamento.setId(id);
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamento.setDataHoraInicio(LocalDateTime.now().plusDays(1));
        agendamento.setDataHoraFim(LocalDateTime.now().plusDays(1).plusHours(1));

        when(agendamentoService.atualizarStatus(eq(id), eq(StatusAgendamento.CANCELADO)))
                .thenReturn(agendamento);

        mockMvc.perform(put(BASE_URL + "/{id}/cancelar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        verify(agendamentoService, times(1)).atualizarStatus(eq(id), eq(StatusAgendamento.CANCELADO));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve listar agendamentos por profissional (HTTP 200)")
    void deveListarAgendamentosPorProfissional() throws Exception {
        UUID profissionalId = UUID.randomUUID();

        mockMvc.perform(get(BASE_URL + "/profissional/{id}", profissionalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
