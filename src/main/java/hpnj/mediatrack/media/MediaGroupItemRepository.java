package hpnj.mediatrack.media;

import hpnj.mediatrack.domain.media.Media;
import hpnj.mediatrack.domain.media.MediaGroup;
import hpnj.mediatrack.domain.media.MediaGroupItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaGroupItemRepository extends JpaRepository<MediaGroupItem, Long> {
    boolean existsByGroupAndMedia(MediaGroup group, Media media);
    int countByGroup(MediaGroup group);
}
