package ifba.engsoft.vidaplena.domain.model.organizacao;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "empresas")
@DiscriminatorValue("EMPRESA")
public class Empresa extends Organizacao {

    @NotBlank(message = "O setor da empresa é obrigatório")
    @Column(nullable = false)
    private String setor;

    // Construtores
    public Empresa() {}

    public Empresa(String nome, String cnpj, String setor) {
        super(nome, cnpj);
        this.setor = setor;
    }

    // Getters e Setters
    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }
}