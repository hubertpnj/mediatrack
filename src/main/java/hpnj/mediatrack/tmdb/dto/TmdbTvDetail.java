package hpnj.mediatrack.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbTvDetail(
        Integer id,
        String name,
        String overview,
        @JsonProperty("first_air_date") String firstAirDate,
        List<TmdbGenre> genres,
        List<TmdbSeasonSummary> seasons
) {}
