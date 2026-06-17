package hpnj.mediatrack.user;

import hpnj.mediatrack.domain.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
