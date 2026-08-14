package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.CriarAgendamentoRequestDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.AgendamentoResponseDTO;
import ifba.engsoft.vidaplena.domain.model.saude.Agendamento;
import ifba.engsoft.vidaplena.domain.model.saude.StatusAgendamento;
import ifba.engsoft.vidaplena.domain.model.saude.TipoAtendimento;
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
class AgendamentoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendamentoService agendamentoService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private static final String BASE_URL_V1 = "/api/v1/agendamentos";
    private static final String BASE_URL = "/api/agendamentos";

    @Test
    @DisplayName("Deve negar acesso a endpoint de agendamento sem autenticação (HTTP 401)")
    void deveNegarAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get(BASE_URL_V1 + "/profissional/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve criar agendamento com sucesso via POST /api/v1/agendamentos (HTTP 201)")
    void deveCriarAgendamentoComSucesso() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        UUID profissionalId = UUID.randomUUID();
        UUID clinicaId = UUID.randomUUID();
        UUID agendamentoId = UUID.randomUUID();
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);

        CriarAgendamentoRequestDTO request = new CriarAgendamentoRequestDTO(
                pacienteId,
                profissionalId,
                clinicaId,
                null,
                dataHora,
                TipoAtendimento.PRESENCIAL,
                "Consulta de Rotina"
        );

        AgendamentoResponseDTO responseDTO = new AgendamentoResponseDTO(
                agendamentoId,
                pacienteId,
                "Paciente Teste",
                profissionalId,
                "Doutor Teste",
                clinicaId,
                "Clínica VidaPlena",
                dataHora,
                StatusAgendamento.AGENDADO,
                TipoAtendimento.PRESENCIAL,
                "Consulta de Rotina"
        );

        when(agendamentoService.criarAgendamento(any(CriarAgendamentoRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post(BASE_URL_V1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(agendamentoId.toString()))
                .andExpect(jsonPath("$.pacienteNome").value("Paciente Teste"))
                .andExpect(jsonPath("$.profissionalNome").value("Doutor Teste"))
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andExpect(jsonPath("$.tipoAtendimento").value("PRESENCIAL"))
                .andExpect(jsonPath("$.observacoes").value("Consulta de Rotina"));

        verify(agendamentoService, times(1)).criarAgendamento(any(CriarAgendamentoRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve listar agendamentos por profissional via GET /api/v1/agendamentos/profissional/{profissionalId} (HTTP 200)")
    void deveListarAgendamentosPorProfissional() throws Exception {
        UUID profissionalId = UUID.randomUUID();
        UUID agendamentoId = UUID.randomUUID();
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);

        AgendamentoResponseDTO responseDTO = new AgendamentoResponseDTO(
                agendamentoId,
                UUID.randomUUID(),
                "Paciente Teste",
                profissionalId,
                "Doutor Teste",
                UUID.randomUUID(),
                "Clínica VidaPlena",
                dataHora,
                StatusAgendamento.AGENDADO,
                TipoAtendimento.PRESENCIAL,
                "Consulta 1"
        );

        when(agendamentoService.listarPorProfissional(profissionalId)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get(BASE_URL_V1 + "/profissional/{id}", profissionalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(agendamentoId.toString()))
                .andExpect(jsonPath("$[0].profissionalId").value(profissionalId.toString()));

        verify(agendamentoService, times(1)).listarPorProfissional(profissionalId);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve confirmar agendamento com sucesso (HTTP 200)")
    void deveConfirmarAgendamentoComSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        Agendamento agendamento = new Agendamento();
        agendamento.setId(id);
        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        agendamento.setDataHora(LocalDateTime.now().plusDays(1));

        AgendamentoResponseDTO responseDTO = new AgendamentoResponseDTO(
                id,
                UUID.randomUUID(),
                "Paciente Teste",
                UUID.randomUUID(),
                "Doutor Teste",
                agendamento.getDataHora(),
                StatusAgendamento.CONFIRMADO
        );

        when(agendamentoService.atualizarStatus(eq(id), eq(StatusAgendamento.CONFIRMADO)))
                .thenReturn(agendamento);
        when(agendamentoService.mapearParaResponseDTO(agendamento))
                .thenReturn(responseDTO);

        mockMvc.perform(put(BASE_URL_V1 + "/{id}/confirmar", id))
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
        agendamento.setDataHora(LocalDateTime.now().plusDays(1));

        AgendamentoResponseDTO responseDTO = new AgendamentoResponseDTO(
                id,
                UUID.randomUUID(),
                "Paciente Teste",
                UUID.randomUUID(),
                "Doutor Teste",
                agendamento.getDataHora(),
                StatusAgendamento.CANCELADO
        );

        when(agendamentoService.atualizarStatus(eq(id), eq(StatusAgendamento.CANCELADO)))
                .thenReturn(agendamento);
        when(agendamentoService.mapearParaResponseDTO(agendamento))
                .thenReturn(responseDTO);

        mockMvc.perform(put(BASE_URL_V1 + "/{id}/cancelar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        verify(agendamentoService, times(1)).atualizarStatus(eq(id), eq(StatusAgendamento.CANCELADO));
    }
}
