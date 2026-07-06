package ifba.engsoft.vidaplena.domain.model.familia;

import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.infrastructure.auditing.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vinculos_dependencia")
public class VinculoDependencia extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O tipo de dependência é obrigatório")
    @Column(nullable = false)
    private TipoDependencia tipo;

    @NotNull(message = "A data de início é obrigatória")
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @ManyToOne
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    @ManyToOne
    @JoinColumn(name = "dependente_id", nullable = false)
    private Usuario dependente;

    // Construtores
    public VinculoDependencia() {}

    public VinculoDependencia(TipoDependencia tipo, LocalDate dataInicio, Usuario responsavel, Usuario dependente) {
        this.tipo = tipo;
        this.dataInicio = dataInicio;
        this.responsavel = responsavel;
        this.dependente = dependente;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TipoDependencia getTipo() {
        return tipo;
    }

    public void setTipo(TipoDependencia tipo) {
        this.tipo = tipo;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Usuario responsavel) {
        this.responsavel = responsavel;
    }

    public Usuario getDependente() {
        return dependente;
    }

    public void setDependente(Usuario dependente) {
        this.dependente = dependente;
    }
}
