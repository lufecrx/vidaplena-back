package ifba.engsoft.vidaplena.infrastructure.mail;

import ifba.engsoft.vidaplena.domain.model.Usuario;

public interface MailService {

	void sendPasswordResetToken(Usuario usuario, String rawToken);
}