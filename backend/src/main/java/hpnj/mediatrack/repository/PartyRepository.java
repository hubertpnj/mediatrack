package hpnj.mediatrack.repository;

import hpnj.mediatrack.domain.party.Party;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PartyRepository extends JpaRepository<Party, UUID> {
}
