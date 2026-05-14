package hpnj.mediatrack.repository;

import hpnj.mediatrack.domain.group.MediaGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaGroupRepository extends JpaRepository<MediaGroup, UUID> {
}
