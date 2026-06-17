package hpnj.mediatrack.auth;

import hpnj.mediatrack.domain.user.UserAccount;
import hpnj.mediatrack.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock OAuth2UserRequest userRequest;
    @Mock OAuth2User oAuth2User;

    private CustomOAuth2UserService serviceWithMockedDelegate() {
        return new CustomOAuth2UserService(userRepository) {
            @Override
            public OAuth2User loadUser(OAuth2UserRequest request) {
                return loadUserFromOAuth2User(oAuth2User);
            }
        };
    }

    @Test
    void existing_user_is_returned_when_email_matches() {
        UserAccount existing = new UserAccount("alice", "alice@example.com", null);
        given(oAuth2User.getAttribute("email")).willReturn("alice@example.com");
        given(oAuth2User.getAttribute("name")).willReturn("Alice Smith");
        given(oAuth2User.getAttributes()).willReturn(Map.of("email", "alice@example.com", "name", "Alice Smith"));
        given(userRepository.findByEmail("alice@example.com")).willReturn(Optional.of(existing));

        OAuth2User result = serviceWithMockedDelegate().loadUser(userRequest);

        assertThat(((OAuth2UserPrincipal) result).account()).isEqualTo(existing);
    }

    @Test
    void new_user_is_created_when_email_not_found() {
        given(oAuth2User.getAttribute("email")).willReturn("bob@example.com");
        given(oAuth2User.getAttribute("name")).willReturn("Bob Jones");
        given(oAuth2User.getAttributes()).willReturn(Map.of("email", "bob@example.com", "name", "Bob Jones"));
        given(userRepository.findByEmail("bob@example.com")).willReturn(Optional.empty());
        given(userRepository.existsByUsername("bobjones")).willReturn(false);
        UserAccount saved = new UserAccount("bobjones", "bob@example.com", null);
        given(userRepository.save(any())).willReturn(saved);

        OAuth2User result = serviceWithMockedDelegate().loadUser(userRequest);

        assertThat(((OAuth2UserPrincipal) result).account().getEmail()).isEqualTo("bob@example.com");
        verify(userRepository).save(any(UserAccount.class));
    }

    @Test
    void username_collision_gets_numeric_suffix() {
        given(oAuth2User.getAttribute("email")).willReturn("carol@example.com");
        given(oAuth2User.getAttribute("name")).willReturn("Carol King");
        given(oAuth2User.getAttributes()).willReturn(Map.of("email", "carol@example.com", "name", "Carol King"));
        given(userRepository.findByEmail("carol@example.com")).willReturn(Optional.empty());
        given(userRepository.existsByUsername("carolking")).willReturn(true);
        given(userRepository.existsByUsername("carolking1")).willReturn(false);
        UserAccount saved = new UserAccount("carolking1", "carol@example.com", null);
        given(userRepository.save(any())).willReturn(saved);

        OAuth2User result = serviceWithMockedDelegate().loadUser(userRequest);

        assertThat(((OAuth2UserPrincipal) result).account().getUsername()).isEqualTo("carolking1");
    }
}
