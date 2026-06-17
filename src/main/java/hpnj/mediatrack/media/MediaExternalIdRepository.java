package hpnj.mediatrack.media;

import hpnj.mediatrack.domain.media.ExternalIdSource;
import hpnj.mediatrack.domain.media.MediaExternalId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaExternalIdRepository extends JpaRepository<MediaExternalId, Long> {
    Optional<MediaExternalId> findBySourceAndExternalId(ExternalIdSource source, String externalId);
    boolean existsBySource(ExternalIdSource source);
}
