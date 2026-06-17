package hpnj.mediatrack.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbMovieDetail(
        Integer id,
        String title,
        String overview,
        @JsonProperty("release_date") String releaseDate,
        Integer runtime,
        List<TmdbGenre> genres
) {}
