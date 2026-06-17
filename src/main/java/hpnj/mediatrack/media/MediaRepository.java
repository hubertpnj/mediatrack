package hpnj.mediatrack.media;

import hpnj.mediatrack.domain.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, Long> {}
