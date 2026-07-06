package ifba.engsoft.vidaplena.infrastructure.auditing;

import org.springframework.data.domain.AuditorAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configuração do Spring Data JPA para auditoria de entidades.
 * Captura automaticamente o usuário autenticado via SecurityContextHolder
 * e fornece ao mecanismo de auditoria do Spring Data JPA.
 * 
 * Quando não há usuário autenticado (ex: tarefas agendadas, logs do sistema),
 * retorna "SISTEMA" como identificador padrão.
 */
@Configuration
public class AuditoriaConfig implements AuditorAware<String> {

    private static final String SISTEMA = "SISTEMA";

    /**
     * Retorna o identificador do usuário auditor atual.
     * Tenta obter do SecurityContextHolder; caso contrário, retorna "SISTEMA".
     *
     * @return Optional contendo o e-mail/nome do usuário ou "SISTEMA"
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of(SISTEMA);
        }

        // Caso especial: usuário anonymous não deve ser tratado como auditor
        if (authentication.getPrincipal() instanceof String &&
            authentication.getPrincipal().equals("anonymousSecurityContextHolder")) {
            return Optional.of(SISTEMA);
        }

        // Retorna o e-mail ou nome do usuário autenticado
        String usuario = authentication.getName();
        
        // Validação adicional: se for anonymous (Spring Security padrão)
        if ("anonymousUser".equals(usuario)) {
            return Optional.of(SISTEMA);
        }

        return Optional.ofNullable(usuario);
    }
}
