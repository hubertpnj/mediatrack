package hpnj.mediatrack.tmdb;

import hpnj.mediatrack.tmdb.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Component
public class TmdbApiClient {

    private static final Logger log = LoggerFactory.getLogger(TmdbApiClient.class);

    private final RestClient restClient;

    public TmdbApiClient(RestClient tmdbRestClient) {
        this.restClient = tmdbRestClient;
    }

    public TmdbMovieDetail fetchMovie(int tmdbId) {
        return restClient.get()
                .uri("/movie/{id}", tmdbId)
                .retrieve()
                .body(TmdbMovieDetail.class);
    }

    public TmdbTvDetail fetchTv(int tmdbId) {
        return restClient.get()
                .uri("/tv/{id}", tmdbId)
                .retrieve()
                .body(TmdbTvDetail.class);
    }

    public TmdbChangesPage fetchMovieChanges(LocalDate startDate, LocalDate endDate, int page) {
        return restClient.get()
                .uri(u -> u.path("/movie/changes")
                        .queryParam("start_date", startDate)
                        .queryParam("end_date", endDate)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(TmdbChangesPage.class);
    }

    public TmdbChangesPage fetchTvChanges(LocalDate startDate, LocalDate endDate, int page) {
        return restClient.get()
                .uri(u -> u.path("/tv/changes")
                        .queryParam("start_date", startDate)
                        .queryParam("end_date", endDate)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(TmdbChangesPage.class);
    }

    public TmdbPopularPage fetchPopularMovies(int page) {
        return restClient.get()
                .uri(u -> u.path("/movie/popular").queryParam("page", page).build())
                .retrieve()
                .body(TmdbPopularPage.class);
    }

    public TmdbPopularPage fetchPopularTv(int page) {
        return restClient.get()
                .uri(u -> u.path("/tv/popular").queryParam("page", page).build())
                .retrieve()
                .body(TmdbPopularPage.class);
    }
}
