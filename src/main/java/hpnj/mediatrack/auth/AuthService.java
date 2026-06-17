package hpnj.mediatrack.auth;

import hpnj.mediatrack.common.ConflictException;
import hpnj.mediatrack.common.UnauthorizedException;
import hpnj.mediatrack.domain.user.UserAccount;
import hpnj.mediatrack.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already registered");
        }
        if (userRepository.existsByUsername(req.username())) {
            throw new ConflictException("Username already taken");
        }
        return userRepository.save(
                new UserAccount(req.username(), req.email(), passwordEncoder.encode(req.password()))
        );
    }

    public UserAccount login(LoginRequest req) {
        UserAccount user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return user;
    }
}
