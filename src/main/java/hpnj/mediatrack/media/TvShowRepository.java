package hpnj.mediatrack.media;

import hpnj.mediatrack.domain.media.TVShow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TvShowRepository extends JpaRepository<TVShow, Long> {
}
