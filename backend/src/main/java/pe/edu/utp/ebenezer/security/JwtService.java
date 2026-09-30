package pe.edu.utp.ebenezer.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import pe.edu.utp.ebenezer.domain.enums.RoleName;

@Service
public class JwtService {

    private static final String ROLE_CLAIM = "role";
    private static final int MIN_KEY_BYTES = 32;

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(JwtProperties properties) {
        this.signingKey = buildSigningKey(properties.secret());
        if (properties.expirationMs() <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION_MS must be greater than zero");
        }
        this.expirationMs = properties.expirationMs();
    }

    public String generateToken(String username, RoleName role) {
        Date issuedAt = new Date();
        return Jwts.builder()
                .subject(username)
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(issuedAt)
                .expiration(new Date(issuedAt.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates signature and expiration and returns the username (subject).
     *
     * @throws JwtException if the token is invalid or expired
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey buildSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured (Base64, at least 256 bits)");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret.trim());
        } catch (DecodingException ex) {
            throw new IllegalStateException("JWT_SECRET must be a valid Base64 value", ex);
        }
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("JWT_SECRET must be at least 256 bits (32 bytes) once decoded");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
