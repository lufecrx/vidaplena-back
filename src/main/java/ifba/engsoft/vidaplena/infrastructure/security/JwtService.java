package ifba.engsoft.vidaplena.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import ifba.engsoft.vidaplena.domain.model.TipoUsuario;
import ifba.engsoft.vidaplena.domain.model.Usuario;

@Service
public class JwtService {

    public static final String ROLE_CLAIM_NAME = "tipos";
    public static final String ROLE_PREFIX = "ROLE_";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gera JWT contendo as roles de domínio com prefixo ROLE_ nas claims.
     * 
     * @param usuario O usuário para o qual o token será gerado.
     * @return O token JWT gerado.
     */
    public String generateToken(Usuario usuario) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.expiration());
        List<String> rolesComPrefixo = usuario.getTipos().stream()
                .map(tipo -> ROLE_PREFIX + tipo.name())
                .toList();

        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiresAt))
                .claim("usuarioId", usuario.getId() != null ? usuario.getId().toString() : null)
                .claim(ROLE_CLAIM_NAME, rolesComPrefixo)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails.getUsername());
    }

    /**
     * Valida o token verificando assinatura, expiração e subject.
     * 
     * @param token O token JWT a ser validado.
     * @param subject O subject esperado (geralmente o email do usuário).
     * @return true se o token for válido, caso contrário, false.
     */
    public boolean isTokenValid(String token, String subject) {
        Claims claims = parseClaims(token);
        return claims.getSubject() != null
                && claims.getSubject().equalsIgnoreCase(subject)
                && claims.getExpiration().toInstant().isAfter(Instant.now());
    }

    /**
     * Extrai as roles de autoridade do token JWT (já com prefixo ROLE_).
     * 
     * @param token O token JWT do qual extrair as roles.
     * @return Um conjunto de strings representando as roles de autoridade.
     */
    public Set<String> extractRoles(String token) {
        Object roles = parseClaims(token).get(ROLE_CLAIM_NAME);
        if (roles instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }

    /**
     * Verifica se o token contém um tipo específico (com ou sem prefixo ROLE_).
     * 
     * @param tipoUsuario O tipo de usuário a ser verificado.
     * @param token O token JWT a ser verificado.
     * @return true se o token contiver o tipo de usuário especificado, caso contrário, false.
     */
    public boolean possuiTipo(TipoUsuario tipoUsuario, String token) {
        String tipoName = tipoUsuario.name();
        Set<String> roles = extractRoles(token);
        return roles.contains(tipoName) || roles.contains(ROLE_PREFIX + tipoName);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}