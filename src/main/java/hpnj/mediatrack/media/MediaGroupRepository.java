package hpnj.mediatrack.media;

import hpnj.mediatrack.domain.media.MediaGroup;
import hpnj.mediatrack.domain.media.MediaGroupType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaGroupRepository extends JpaRepository<MediaGroup, Long> {
    Optional<MediaGroup> findByNameAndGroupType(String name, MediaGroupType groupType);
}
