package hpnj.mediatrack.media;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaService {

    private final MediaRepository repository;

    public MediaService(MediaRepository repository) {
        this.repository = repository;
    }

    public List<MediaSummary> findAll() {
        return repository.findAll().stream()
                .map(m -> new MediaSummary(
                        m.getId(),
                        m.getTitle(),
                        m.getClass().getSimpleName(),
                        m.getReleaseDate()))
                .toList();
    }
}
