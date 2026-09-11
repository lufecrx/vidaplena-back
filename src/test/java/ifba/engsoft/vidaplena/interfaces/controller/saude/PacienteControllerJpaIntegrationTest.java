package ifba.engsoft.vidaplena.interfaces.controller.saude;

import ifba.engsoft.vidaplena.domain.model.StatusUsuario;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.domain.model.saude.Paciente;
import ifba.engsoft.vidaplena.domain.model.saude.TipoSanguineo;
import ifba.engsoft.vidaplena.domain.repository.UsuarioRepository;
import ifba.engsoft.vidaplena.domain.repository.saude.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PacienteControllerJpaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        pacienteRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = new Usuario(
                "Carlos Silva",
                "12345678909",
                "carlos.silva@vidaplena.test",
                "$2a$10$xyz",
                "71999998888",
                LocalDate.of(1990, 5, 20),
                StatusUsuario.ATIVO,
                Set.of(TipoUsuario.PACIENTE)
        );
        usuario = usuarioRepository.save(usuario);

        paciente = new Paciente(usuario);
        paciente.setTipoSanguineo(TipoSanguineo.AB_POSITIVO);
        paciente.setAlergias(new ArrayList<>(List.of("Dipirona", "Penicilina")));
        paciente.setMedicamentosContinuos(new ArrayList<>(List.of("Losartana 50mg")));
        paciente.setHistoricoFamiliar("Histórico familiar de cardiopatia.");
        paciente = pacienteRepository.save(paciente);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Deve listar todos os pacientes com sucesso via /api/v1/pacientes serializando coleções LAZY com open-in-view desativado")
    void deveListarPacientesV1ComSucesso() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(paciente.getId().toString())))
                .andExpect(jsonPath("$[0].usuarioId", is(usuario.getId().toString())))
                .andExpect(jsonPath("$[0].tipoSanguineo", is("AB_POSITIVO")))
                .andExpect(jsonPath("$[0].alergias", containsInAnyOrder("Dipirona", "Penicilina")))
                .andExpect(jsonPath("$[0].medicamentosContinuos", contains("Losartana 50mg")))
                .andExpect(jsonPath("$[0].historicoFamiliar", is("Histórico familiar de cardiopatia.")))
                .andExpect(jsonPath("$[0].usuario.nome", is("Carlos Silva")))
                .andExpect(jsonPath("$[0].usuario.email", is("carlos.silva@vidaplena.test")));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve listar todos os pacientes com sucesso via rota legada /api/pacientes")
    void deveListarPacientesRotaLegadaComSucesso() throws Exception {
        mockMvc.perform(get("/api/pacientes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(paciente.getId().toString())))
                .andExpect(jsonPath("$[0].alergias", hasSize(2)))
                .andExpect(jsonPath("$[0].usuario.cpf", is("12345678909")));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve obter paciente por ID com sucesso serializando coleções LAZY sem erro")
    void deveObterPacientePorIdComSucesso() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes/{id}", paciente.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(paciente.getId().toString())))
                .andExpect(jsonPath("$.tipoSanguineo", is("AB_POSITIVO")))
                .andExpect(jsonPath("$.alergias", containsInAnyOrder("Dipirona", "Penicilina")))
                .andExpect(jsonPath("$.medicamentosContinuos", contains("Losartana 50mg")))
                .andExpect(jsonPath("$.usuario.nome", is("Carlos Silva")));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve obter paciente por usuarioId com sucesso serializando coleções LAZY sem erro")
    void deveObterPacientePorUsuarioIdComSucesso() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes/usuario/{usuarioId}", usuario.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(paciente.getId().toString())))
                .andExpect(jsonPath("$.usuarioId", is(usuario.getId().toString())))
                .andExpect(jsonPath("$.alergias", containsInAnyOrder("Dipirona", "Penicilina")))
                .andExpect(jsonPath("$.usuario.email", is("carlos.silva@vidaplena.test")));
    }
}
