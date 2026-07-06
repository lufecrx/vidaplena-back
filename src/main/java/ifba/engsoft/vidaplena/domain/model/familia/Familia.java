package ifba.engsoft.vidaplena.domain.model.familia;

import ifba.engsoft.vidaplena.domain.model.Usuario;
import ifba.engsoft.vidaplena.infrastructure.auditing.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "familias")
public class Familia extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "O nome da família é obrigatório")
    @Column(nullable = false)
    private String nome;

    @ManyToMany
    @JoinTable(
        name = "familia_usuarios",
        joinColumns = @JoinColumn(name = "familia_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<Usuario> membros;

    // Construtores
    public Familia() {}

    public Familia(String nome) {
        this.nome = nome;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Usuario> getMembros() {
        return membros;
    }

    public void setMembros(List<Usuario> membros) {
        this.membros = membros;
    }
}
