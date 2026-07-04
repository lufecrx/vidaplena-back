package ifba.engsoft.vidaplena.interfaces.dto.auth;

import ifba.engsoft.vidaplena.interfaces.dto.usuario.UsuarioResponse;

public record AuthResponse(String accessToken, String tokenType, UsuarioResponse usuario) {
}