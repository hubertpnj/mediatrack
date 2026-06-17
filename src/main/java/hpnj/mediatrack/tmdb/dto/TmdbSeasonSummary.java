package hpnj.mediatrack.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbSeasonSummary(
        Integer id,
        @JsonProperty("season_number") Integer seasonNumber,
        String name,
        @JsonProperty("air_date") String airDate
) {}
