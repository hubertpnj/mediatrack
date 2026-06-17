package hpnj.mediatrack.tmdb;

import hpnj.mediatrack.media.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "tmdb.sync.enabled", havingValue = "true", matchIfMissing = true)
public class TmdbScheduler {

    private static final Logger log = LoggerFactory.getLogger(TmdbScheduler.class);

    private final TmdbSyncService syncService;
    private final MovieRepository movieRepo;

    public TmdbScheduler(TmdbSyncService syncService, MovieRepository movieRepo) {
        this.syncService = syncService;
        this.movieRepo = movieRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        if (!movieRepo.existsByTmdbIdIsNotNull()) {
            log.info("No TMDB data found — running initial import");
            syncService.initialImport();
        } else {
            log.info("TMDB data already present — skipping initial import");
        }
    }

    @Scheduled(cron = "${tmdb.sync.cron:0 0 3 * * *}")
    public void dailySync() {
        syncService.dailySync();
    }
}
