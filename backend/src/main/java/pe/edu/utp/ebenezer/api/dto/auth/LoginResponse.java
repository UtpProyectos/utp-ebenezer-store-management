package pe.edu.utp.ebenezer.api.dto.auth;

import pe.edu.utp.ebenezer.api.dto.user.UserResponse;

// expiresIn is expressed in seconds.
public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
