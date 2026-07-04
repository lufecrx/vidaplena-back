package ifba.engsoft.vidaplena.domain.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(name = "token_hash", nullable = false, unique = true, length = 128)
	private String tokenHash;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "used_at")
	private Instant usedAt;

	protected PasswordResetToken() {
	}

	public PasswordResetToken(Usuario usuario, String tokenHash, Instant createdAt, Instant expiresAt) {
		this.usuario = usuario;
		this.tokenHash = tokenHash;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
	}

	public UUID getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getUsedAt() {
		return usedAt;
	}

	public boolean isExpired(Instant now) {
		return now.isAfter(expiresAt);
	}

	public boolean isConsumed() {
		return usedAt != null;
	}

	public void markAsUsed(Instant now) {
		this.usedAt = now;
	}
}