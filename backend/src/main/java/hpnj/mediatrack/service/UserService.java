package hpnj.mediatrack.service;

import hpnj.mediatrack.domain.enums.UserRole;
import hpnj.mediatrack.domain.user.UserAccount;
import hpnj.mediatrack.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount createUser(String username, String email, String rawPassword, UserRole role) {
        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userAccountRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<UserAccount> findByIdOrEmail(String idOrEmail) {
        try {
            return userAccountRepository.findById(UUID.fromString(idOrEmail));
        } catch (IllegalArgumentException e) {
            return userAccountRepository.findByEmail(idOrEmail);
        }
    }

    @Transactional
    public void changeRole(UUID userId, UserRole newRole) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setRole(newRole);
        userAccountRepository.save(user);
    }

    @Transactional
    public void suspend(UUID userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setSuspended(true);
        userAccountRepository.save(user);
    }

    @Transactional
    public void delete(UUID userId) {
        userAccountRepository.deleteById(userId);
    }
}
