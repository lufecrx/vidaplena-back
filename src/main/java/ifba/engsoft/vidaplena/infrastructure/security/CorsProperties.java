package ifba.engsoft.vidaplena.infrastructure.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração para Cross-Origin Resource Sharing (CORS).
 * Permite definir e parametrizar as origens permitidas via application.yaml / variáveis de ambiente.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        List<String> allowedOrigins
) {
    public CorsProperties {
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            allowedOrigins = List.of("http://localhost:3000");
        }
    }
}
