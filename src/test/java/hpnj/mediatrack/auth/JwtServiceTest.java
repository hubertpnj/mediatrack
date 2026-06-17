package hpnj.mediatrack.auth;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long!!";
    private final JwtService jwtService = new JwtService(SECRET);

    @Test
    void roundtrip_userId_survives_generate_and_extract() {
        String token = jwtService.generate(42L);
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void extractUserId_throws_on_invalid_token() {
        assertThatThrownBy(() -> jwtService.extractUserId("not.a.token"))
                .isInstanceOf(Exception.class);
    }
}
