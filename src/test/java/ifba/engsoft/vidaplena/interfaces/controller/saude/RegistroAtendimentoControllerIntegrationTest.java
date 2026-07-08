package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.dto.saude.RegistroAtendimentoDTO;
import ifba.engsoft.vidaplena.domain.dto.saude.NotaRetificacaoDTO;
import ifba.engsoft.vidaplena.domain.model.saude.*;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistroAtendimentoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProntuarioService prontuarioService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private static final String BASE_URL = "/api/registros-atendimento";

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
    @DisplayName("Deve permitir criar registro de atendimento para perfil MEDICO (HTTP 201)")
    void devePermitirCriarRegistroParaMedico() throws Exception {
        UUID prontuarioId = UUID.randomUUID();
        UUID profissionalId = UUID.randomUUID();
        UUID agendamentoId = UUID.randomUUID();

        RegistroAtendimentoDTO dto = new RegistroAtendimentoDTO(
                null,
                prontuarioId,
                profissionalId,
                agendamentoId,
                "Febre",
                "Gripe",
                "Paracetamol",
                "Repouso",
                "Nota clinica",
                true
        );

        Prontuario prontuario = new Prontuario();
        prontuario.setId(prontuarioId);
        Profissional profissional = new Profissional();
        profissional.setId(profissionalId);
        Agendamento agendamento = new Agendamento();
        agendamento.setId(agendamentoId);

        RegistroAtendimento registro = new RegistroAtendimento(prontuario, profissional, agendamento);
        registro.setId(UUID.randomUUID());
        registro.setSintomasRelatados("Febre");
        registro.setDiagnostico("Gripe");
        registro.setPrescricaoMedica("Paracetamol");
        registro.setPrescricaoEnfermagem("Repouso");
        registro.setNotasClinicas("Nota clinica");
        registro.setFinalizado(true);

        when(prontuarioService.criarRegistroAtendimento(any(RegistroAtendimento.class))).thenReturn(registro);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sintomasRelatados").value("Febre"))
                .andExpect(jsonPath("$.finalizado").value(true));

        verify(prontuarioService, times(1)).criarRegistroAtendimento(any(RegistroAtendimento.class));
    }

    @Test
    @WithMockUser(roles = "NUTRICIONISTA")
    @DisplayName("Deve permitir atualizar registro de atendimento para perfil NUTRICIONISTA (HTTP 200)")
    void devePermitirAtualizarRegistroParaNutricionista() throws Exception {
        UUID id = UUID.randomUUID();
        RegistroAtendimentoDTO dto = new RegistroAtendimentoDTO(
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Nova queixa",
                "Novo diagnóstico",
                "Nova prescrição",
                "Novo cuidado",
                "Nota",
                false
        );

        RegistroAtendimento registro = new RegistroAtendimento();
        registro.setId(id);
        registro.setSintomasRelatados("Nova queixa");
        registro.setDiagnostico("Novo diagnóstico");
        
        Prontuario prontuario = new Prontuario();
        prontuario.setId(UUID.randomUUID());
        registro.setProntuario(prontuario);
        
        Profissional profissional = new Profissional();
        profissional.setId(UUID.randomUUID());
        registro.setProfissional(profissional);
        
        Agendamento agendamento = new Agendamento();
        agendamento.setId(UUID.randomUUID());
        registro.setAgendamento(agendamento);

        when(prontuarioService.atualizarRegistroAtendimento(eq(id), any(RegistroAtendimento.class)))
                .thenReturn(registro);

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sintomasRelatados").value("Nova queixa"));

        verify(prontuarioService, times(1)).atualizarRegistroAtendimento(eq(id), any(RegistroAtendimento.class));
    }

    @Test
    @WithMockUser(roles = "PERSONAL_TRAINER")
    @DisplayName("Deve permitir deletar registro de atendimento para perfil PERSONAL_TRAINER (HTTP 204)")
    void devePermitirDeletarRegistroParaPersonalTrainer() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(prontuarioService).deletarRegistroAtendimento(id);

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNoContent());

        verify(prontuarioService, times(1)).deletarRegistroAtendimento(id);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve permitir retificar registro de atendimento para perfil MEDICO (HTTP 201)")
    void devePermitirRetificarRegistroParaMedico() throws Exception {
        UUID id = UUID.randomUUID();
        UUID profissionalId = UUID.randomUUID();
        NotaRetificacaoDTO dto = new NotaRetificacaoDTO(id, profissionalId, "Texto de retificação");

        RegistroAtendimento registro = new RegistroAtendimento();
        registro.setId(id);
        Profissional profissional = new Profissional();
        profissional.setId(profissionalId);

        NotaRetificacao nota = new NotaRetificacao(registro, profissional, "Texto de retificação");
        nota.setId(UUID.randomUUID());

        when(prontuarioService.adicionarNotaRetificacao(eq(id), eq(profissionalId), eq("Texto de retificação")))
                .thenReturn(nota);

        mockMvc.perform(post(BASE_URL + "/{id}/retificar", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.texto").value("Texto de retificação"));

        verify(prontuarioService, times(1)).adicionarNotaRetificacao(eq(id), eq(profissionalId), eq("Texto de retificação"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Deve obter registro de atendimento por ID para perfil ADMINISTRADOR (HTTP 200)")
    void deveObterRegistroParaAdministrador() throws Exception {
        UUID id = UUID.randomUUID();
        RegistroAtendimento registro = new RegistroAtendimento();
        registro.setId(id);
        
        Prontuario prontuario = new Prontuario();
        prontuario.setId(UUID.randomUUID());
        registro.setProntuario(prontuario);
        
        Profissional profissional = new Profissional();
        profissional.setId(UUID.randomUUID());
        registro.setProfissional(profissional);
        
        Agendamento agendamento = new Agendamento();
        agendamento.setId(UUID.randomUUID());
        registro.setAgendamento(agendamento);

        when(prontuarioService.obterRegistroAtendimentoPorId(id)).thenReturn(registro);

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(prontuarioService, times(1)).obterRegistroAtendimentoPorId(id);
    }
}
