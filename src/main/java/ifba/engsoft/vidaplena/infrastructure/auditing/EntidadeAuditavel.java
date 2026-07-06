package ifba.engsoft.vidaplena.infrastructure.auditing;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Classe abstrata base para todas as entidades do domínio que requerem auditoria automática.
 * Utiliza o mecanismo nativo do Spring Data JPA para capturar metadados de criação e modificação.
 * 
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadeAuditavel {

    /**
     * Usuário que criou o registro.
     */
    @CreatedBy
    @Column(name = "criado_por", updatable = false, nullable = false, length = 100)
    private String criadoPor;

    /**
     * Data/hora de criação do registro.
     */
    @CreatedDate
    @Column(name = "data_criacao", updatable = false, nullable = false)
    private LocalDateTime dataCriacao;

    /**
     * Usuário que modificou o registro pela última vez.
     */
    @LastModifiedBy
    @Column(name = "modificado_por", length = 100)
    private String modificadoPor;

    /**
     * Data/hora da última modificação. 
     */
    @LastModifiedDate
    @Column(name = "data_modificacao")
    private LocalDateTime dataModificacao;

    public String getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(String criadoPor) {
        this.criadoPor = criadoPor;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getModificadoPor() {
        return modificadoPor;
    }

    public void setModificadoPor(String modificadoPor) {
        this.modificadoPor = modificadoPor;
    }

    public LocalDateTime getDataModificacao() {
        return dataModificacao;
    }

    public void setDataModificacao(LocalDateTime dataModificacao) {
        this.dataModificacao = dataModificacao;
    }
}
