package hpnj.mediatrack.tmdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TmdbConfig {

    @Bean
    RestClient tmdbRestClient(
            @Value("${tmdb.api.key}") String apiKey,
            @Value("${tmdb.base-url:https://api.themoviedb.org/3}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
