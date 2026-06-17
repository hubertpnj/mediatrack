package hpnj.mediatrack.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbPopularPage(
        List<TmdbPopularItem> results,
        int page,
        @JsonProperty("total_pages") int totalPages
) {}
