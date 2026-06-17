package hpnj.mediatrack.media;

import java.time.LocalDate;

public record MediaSummary(Long id, String title, String type, LocalDate releaseDate) {}
