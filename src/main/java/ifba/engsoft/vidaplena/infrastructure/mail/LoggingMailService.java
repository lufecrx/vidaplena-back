package ifba.engsoft.vidaplena.infrastructure.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ifba.engsoft.vidaplena.domain.model.Usuario;

@Service
public class LoggingMailService implements MailService {

	private static final Logger log = LoggerFactory.getLogger(LoggingMailService.class);

	@Override
	public void sendPasswordResetToken(Usuario usuario, String rawToken) {
		log.info("Password reset token generated for {}: {}", usuario.getEmail(), rawToken);
	}
}