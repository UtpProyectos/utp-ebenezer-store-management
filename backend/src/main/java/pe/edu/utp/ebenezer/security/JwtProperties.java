package pe.edu.utp.ebenezer.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param secret       Base64-encoded HMAC key (at least 256 bits), from JWT_SECRET
 * @param expirationMs token lifetime in milliseconds, from JWT_EXPIRATION_MS
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMs) {
}
