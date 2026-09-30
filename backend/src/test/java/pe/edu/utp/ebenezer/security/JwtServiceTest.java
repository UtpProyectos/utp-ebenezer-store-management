package pe.edu.utp.ebenezer.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import pe.edu.utp.ebenezer.domain.enums.RoleName;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes());
    private static final String OTHER_SECRET = Base64.getEncoder()
            .encodeToString("fedcba9876543210fedcba9876543210".getBytes());

    @Test
    void generatesTokenThatResolvesToUsername() {
        JwtService jwtService = new JwtService(new JwtProperties(SECRET, 60_000));

        String token = jwtService.generateToken("admin", RoleName.ADMIN);

        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(60);
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String token = new JwtService(new JwtProperties(OTHER_SECRET, 60_000)).generateToken("admin", RoleName.ADMIN);
        JwtService jwtService = new JwtService(new JwtProperties(SECRET, 60_000));

        assertThatThrownBy(() -> jwtService.extractUsername(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtService jwtService = new JwtService(new JwtProperties(SECRET, 1));
        String token = jwtService.generateToken("admin", RoleName.ADMIN);
        Thread.sleep(1_100);

        assertThatThrownBy(() -> jwtService.extractUsername(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void failsFastWithMissingOrShortSecret() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("", 60_000)))
                .isInstanceOf(IllegalStateException.class);
        String shortSecret = Base64.getEncoder().encodeToString("too-short".getBytes());
        assertThatThrownBy(() -> new JwtService(new JwtProperties(shortSecret, 60_000)))
                .isInstanceOf(IllegalStateException.class);
    }
}
