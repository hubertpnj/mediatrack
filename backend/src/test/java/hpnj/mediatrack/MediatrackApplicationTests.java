package hpnj.mediatrack;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires running PostgreSQL, Redis and Kafka — run with docker compose up")
class MediatrackApplicationTests {

    @Test
    void contextLoads() {
    }
}
