package ifba.engsoft.vidaplena.infrastructure.mail;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import ifba.engsoft.vidaplena.domain.model.Usuario;

@Service
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
@ConditionalOnBean(JavaMailSender.class)
public class SmtpMailService implements MailService {

	private final JavaMailSender mailSender;

	public SmtpMailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Override
	public void sendPasswordResetToken(Usuario usuario, String rawToken) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(usuario.getEmail());
		message.setSubject("Recuperação de senha - VidaPlena");
		message.setText("Use este token para redefinir sua senha: " + rawToken);
		mailSender.send(message);
	}
}