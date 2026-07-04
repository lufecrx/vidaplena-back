package ifba.engsoft.vidaplena.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

	private final JwtProperties properties;
	private final SecretKey secretKey;

	public JwtService(JwtProperties properties) {
		this.properties = properties;
		this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(Usuario usuario) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(properties.expiration());
		List<String> tipos = usuario.getTipos().stream().map(Enum::name).toList();

		return Jwts.builder()
				.setSubject(usuario.getEmail())
				.setIssuedAt(Date.from(issuedAt))
				.setExpiration(Date.from(expiresAt))
				.claim("usuarioId", usuario.getId() != null ? usuario.getId().toString() : null)
				.claim("tipos", tipos)
				.signWith(secretKey, SignatureAlgorithm.HS256)
				.compact();
	}

	public String extractSubject(String token) {
		return parseClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		Claims claims = parseClaims(token);
		return claims.getSubject() != null && claims.getSubject().equalsIgnoreCase(userDetails.getUsername())
				&& claims.getExpiration().toInstant().isAfter(Instant.now());
	}

	@SuppressWarnings("unchecked")
	public Set<String> extractRoles(String token) {
		Object roles = parseClaims(token).get("tipos");
		if (roles instanceof List<?> list) {
			return list.stream().map(String::valueOf).collect(java.util.stream.Collectors.toSet());
		}
		return Set.of();
	}

	public boolean possuiTipo(TipoUsuario tipoUsuario, String token) {
		return extractRoles(token).contains(tipoUsuario.name());
	}

	private Claims parseClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
}