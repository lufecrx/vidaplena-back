package ifba.engsoft.vidaplena.domain.model.organizacao;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "clinicas")
@DiscriminatorValue("CLINICA")
public class Clinica extends Organizacao {

    @NotBlank(message = "O tipo de clínica é obrigatório")
    @Column(nullable = false)
    private String tipo;

    // Construtores
    public Clinica() {}

    public Clinica(String nome, String cnpj, String tipo) {
        super(nome, cnpj);
        this.tipo = tipo;
    }

    // Getters e Setters
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}