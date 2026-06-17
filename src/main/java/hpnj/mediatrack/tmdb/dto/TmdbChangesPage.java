package hpnj.mediatrack.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbChangesPage(
        List<TmdbChangeItem> results,
        int page,
        @JsonProperty("total_pages") int totalPages
) {}
