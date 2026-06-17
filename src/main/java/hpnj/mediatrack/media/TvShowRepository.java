package hpnj.mediatrack.media;

import hpnj.mediatrack.domain.media.TVShow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TvShowRepository extends JpaRepository<TVShow, Long> {
    Optional<TVShow> findByTmdbId(Integer tmdbId);
}
