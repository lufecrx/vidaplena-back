package ifba.engsoft.vidaplena.domain.model.organizacao;

import ifba.engsoft.vidaplena.infrastructure.auditing.EntidadeAuditavel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Organizacao extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "O nome da organização é obrigatório")
    @Column(nullable = false)
    private String nome;

    @Pattern(regexp = "^\\d{14}$", message = "O CNPJ deve conter 14 dígitos")
    @Column(name = "cnpj", nullable = false, unique = true)
    private String cnpj;

    // Construtores
    public Organizacao() {}

    public Organizacao(String nome, String cnpj) {
        this.nome = nome;
        this.cnpj = cnpj;
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

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }
}
