package hpnj.mediatrack.tmdb;

import hpnj.mediatrack.domain.media.*;
import hpnj.mediatrack.media.*;
import hpnj.mediatrack.tmdb.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TmdbSyncService {

    private static final Logger log = LoggerFactory.getLogger(TmdbSyncService.class);

    private final TmdbApiClient apiClient;
    private final MovieRepository movieRepo;
    private final TvShowRepository tvShowRepo;
    private final MediaGroupRepository mediaGroupRepo;
    private final MediaGroupItemRepository mediaGroupItemRepo;
    private final MediaExternalIdRepository externalIdRepo;

    @Value("${tmdb.sync.popular-pages:5}")
    private int popularPages;

    public TmdbSyncService(TmdbApiClient apiClient,
                           MovieRepository movieRepo,
                           TvShowRepository tvShowRepo,
                           MediaGroupRepository mediaGroupRepo,
                           MediaGroupItemRepository mediaGroupItemRepo,
                           MediaExternalIdRepository externalIdRepo) {
        this.apiClient = apiClient;
        this.movieRepo = movieRepo;
        this.tvShowRepo = tvShowRepo;
        this.mediaGroupRepo = mediaGroupRepo;
        this.mediaGroupItemRepo = mediaGroupItemRepo;
        this.externalIdRepo = externalIdRepo;
    }

    public void initialImport() {
        log.info("Starting TMDB initial import ({} pages of popular movies and TV shows)", popularPages);
        for (int page = 1; page <= popularPages; page++) {
            importPopularMoviesPage(page);
            importPopularTvPage(page);
            throttle();
        }
        log.info("TMDB initial import complete");
    }

    public void dailySync() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(1);
        log.info("Starting TMDB daily sync for changes between {} and {}", startDate, endDate);
        syncMovieChanges(startDate, endDate);
        syncTvChanges(startDate, endDate);
        log.info("TMDB daily sync complete");
    }

    private void importPopularMoviesPage(int page) {
        TmdbPopularPage popular = apiClient.fetchPopularMovies(page);
        for (TmdbPopularItem item : popular.results()) {
            try {
                TmdbMovieDetail detail = apiClient.fetchMovie(item.id());
                upsertMovie(detail);
                throttle();
            } catch (Exception e) {
                log.warn("Failed to import movie tmdbId={}: {}", item.id(), e.getMessage());
            }
        }
    }

    private void importPopularTvPage(int page) {
        TmdbPopularPage popular = apiClient.fetchPopularTv(page);
        for (TmdbPopularItem item : popular.results()) {
            try {
                TmdbTvDetail detail = apiClient.fetchTv(item.id());
                upsertTv(detail);
                throttle();
            } catch (Exception e) {
                log.warn("Failed to import TV show tmdbId={}: {}", item.id(), e.getMessage());
            }
        }
    }

    private void syncMovieChanges(LocalDate startDate, LocalDate endDate) {
        int page = 1;
        int totalPages;
        do {
            TmdbChangesPage changes = apiClient.fetchMovieChanges(startDate, endDate, page);
            totalPages = changes.totalPages();
            for (TmdbChangeItem item : changes.results()) {
                if (item.adult()) continue;
                try {
                    TmdbMovieDetail detail = apiClient.fetchMovie(item.id());
                    upsertMovie(detail);
                    throttle();
                } catch (Exception e) {
                    log.warn("Failed to sync movie tmdbId={}: {}", item.id(), e.getMessage());
                }
            }
            page++;
        } while (page <= totalPages);
    }

    private void syncTvChanges(LocalDate startDate, LocalDate endDate) {
        int page = 1;
        int totalPages;
        do {
            TmdbChangesPage changes = apiClient.fetchTvChanges(startDate, endDate, page);
            totalPages = changes.totalPages();
            for (TmdbChangeItem item : changes.results()) {
                if (item.adult()) continue;
                try {
                    TmdbTvDetail detail = apiClient.fetchTv(item.id());
                    upsertTv(detail);
                    throttle();
                } catch (Exception e) {
                    log.warn("Failed to sync TV show tmdbId={}: {}", item.id(), e.getMessage());
                }
            }
            page++;
        } while (page <= totalPages);
    }

    private void upsertMovie(TmdbMovieDetail dto) {
        LocalDate releaseDate = parseDate(dto.releaseDate());
        String externalId = String.valueOf(dto.id());

        Optional<MediaExternalId> existing = externalIdRepo.findBySourceAndExternalId(ExternalIdSource.TMDB, externalId);
        Movie movie;
        if (existing.isPresent()) {
            movie = (Movie) existing.get().getMedia();
        } else {
            movie = movieRepo.save(new Movie(dto.title(), releaseDate, dto.runtime()));
            externalIdRepo.save(new MediaExternalId(movie, ExternalIdSource.TMDB, externalId));
        }

        movie.setTitle(dto.title());
        movie.setReleaseDate(releaseDate);
        movie.setDurationMinutes(dto.runtime());
        movieRepo.save(movie);
        linkGenres(movie, dto.genres());
    }

    private void upsertTv(TmdbTvDetail dto) {
        LocalDate releaseDate = parseDate(dto.firstAirDate());
        String externalId = String.valueOf(dto.id());

        Optional<MediaExternalId> existing = externalIdRepo.findBySourceAndExternalId(ExternalIdSource.TMDB, externalId);
        TVShow show;
        if (existing.isPresent()) {
            show = (TVShow) existing.get().getMedia();
        } else {
            show = tvShowRepo.save(new TVShow(dto.name(), releaseDate));
            externalIdRepo.save(new MediaExternalId(show, ExternalIdSource.TMDB, externalId));
        }

        show.setTitle(dto.name());
        show.setReleaseDate(releaseDate);
        show = tvShowRepo.save(show);
        linkGenres(show, dto.genres());
        if (dto.seasons() != null) {
            syncSeasons(show, dto.seasons());
        }
    }

    private void linkGenres(Media media, List<TmdbGenre> genres) {
        if (genres == null) return;
        for (TmdbGenre g : genres) {
            MediaGroup group = upsertGenre(g.name());
            if (!mediaGroupItemRepo.existsByGroupAndMedia(group, media)) {
                int position = mediaGroupItemRepo.countByGroup(group) + 1;
                mediaGroupItemRepo.save(new MediaGroupItem(group, media, position));
            }
        }
    }

    private MediaGroup upsertGenre(String name) {
        return mediaGroupRepo.findByNameAndGroupType(name, MediaGroupType.GENRE)
                .orElseGet(() -> mediaGroupRepo.save(new MediaGroup(name, MediaGroupType.GENRE, null)));
    }

    private void syncSeasons(TVShow show, List<TmdbSeasonSummary> tmdbSeasons) {
        for (TmdbSeasonSummary s : tmdbSeasons) {
            if (s.seasonNumber() == null || s.seasonNumber() == 0) continue;
            boolean exists = show.getSeasons().stream()
                    .anyMatch(existing -> existing.getSeasonNumber().equals(s.seasonNumber()));
            if (!exists) {
                show.getSeasons().add(new Season(show, s.seasonNumber(), s.name(), parseDate(s.airDate())));
            }
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    private void throttle() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
