package hpnj.mediatrack.service;

import hpnj.mediatrack.domain.enums.UserRole;
import hpnj.mediatrack.domain.user.UserAccount;
import hpnj.mediatrack.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserAccountRepository userAccountRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    @Test
    void createUser_savesWithHashedPassword() {
        when(passwordEncoder.encode("secret")).thenReturn("$hashed$");
        when(userAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.createUser("jan", "jan@example.com", "secret", UserRole.USER);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        UserAccount saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("$hashed$");
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.getEmail()).isEqualTo("jan@example.com");
    }

    @Test
    void createUser_throwsOnDuplicateEmail() {
        when(userAccountRepository.existsByEmail("jan@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("jan", "jan@example.com", "pass", UserRole.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void changeRole_updatesRole() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setRole(UserRole.USER);
        when(userAccountRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.changeRole(user.getId(), UserRole.MODERATOR);

        assertThat(user.getRole()).isEqualTo(UserRole.MODERATOR);
    }

    @Test
    void suspend_setsSuspendedTrue() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setSuspended(false);
        when(userAccountRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.suspend(user.getId());

        assertThat(user.isSuspended()).isTrue();
    }
}
