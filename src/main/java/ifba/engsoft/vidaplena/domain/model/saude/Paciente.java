package ifba.engsoft.vidaplena.domain.model.saude;

import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.infrastructure.auditing.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "pacientes")
public class Paciente extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "O ID do usuário é obrigatório")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sanguineo")
    private TipoSanguineo tipoSanguineo;

    @ElementCollection
    @CollectionTable(name = "paciente_alergias", joinColumns = @JoinColumn(name = "paciente_id"))
    @Column(name = "alergia")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<String> alergias;

    @ElementCollection
    @CollectionTable(name = "paciente_medicamentos_continuos", joinColumns = @JoinColumn(name = "paciente_id"))
    @Column(name = "medicamento_continuo")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<String> medicamentosContinuos;

    @Column(name = "historico_familiar", length = 10000)
    @Size(max = 10000, message = "O histórico familiar não pode exceder 10000 caracteres")
    private String historicoFamiliar;

    @Column(name = "peso")
    private Double peso;

    @Column(name = "pressao", length = 20)
    private String pressao;

    @Column(name = "glicemia")
    private Double glicemia;

    @Column(name = "horas_sono")
    private Double horasSono;

    @Column(name = "agua")
    private Double agua;

    @Column(name = "atividade_fisica", length = 1000)
    private String atividadeFisica;

    // Construtores
    public Paciente() {}

    public Paciente(Usuario usuario) {
        this.usuario = usuario;
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public TipoSanguineo getTipoSanguineo() { return tipoSanguineo; }
    public void setTipoSanguineo(TipoSanguineo tipoSanguineo) { this.tipoSanguineo = tipoSanguineo; }

    public List<String> getAlergias() { return alergias; }
    public void setAlergias(List<String> alergias) { this.alergias = alergias; }

    public List<String> getMedicamentosContinuos() { return medicamentosContinuos; }
    public void setMedicamentosContinuos(List<String> medicamentosContinuos) { this.medicamentosContinuos = medicamentosContinuos; }

    public String getHistoricoFamiliar() { return historicoFamiliar; }
    public void setHistoricoFamiliar(String historicoFamiliar) { this.historicoFamiliar = historicoFamiliar; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public String getPressao() { return pressao; }
    public void setPressao(String pressao) { this.pressao = pressao; }

    public Double getGlicemia() { return glicemia; }
    public void setGlicemia(Double glicemia) { this.glicemia = glicemia; }

    public Double getHorasSono() { return horasSono; }
    public void setHorasSono(Double horasSono) { this.horasSono = horasSono; }

    public Double getAgua() { return agua; }
    public void setAgua(Double agua) { this.agua = agua; }

    public String getAtividadeFisica() { return atividadeFisica; }
    public void setAtividadeFisica(String atividadeFisica) { this.atividadeFisica = atividadeFisica; }
}