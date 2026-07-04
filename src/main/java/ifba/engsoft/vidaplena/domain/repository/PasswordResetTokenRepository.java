package ifba.engsoft.vidaplena.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ifba.engsoft.vidaplena.domain.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

	Optional<PasswordResetToken> findFirstByUsuario_IdAndUsedAtIsNullOrderByCreatedAtDesc(UUID usuarioId);

	Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNull(String tokenHash);
}