package hpnj.mediatrack.auth;

import hpnj.mediatrack.domain.user.UserAccount;
import hpnj.mediatrack.user.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = delegate.loadUser(request);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        UserAccount user = userRepository.findByEmail(email).orElseGet(() -> {
            String base = name != null
                    ? name.replaceAll("\\s+", "").toLowerCase()
                    : email.split("@")[0];
            String username = base;
            int suffix = 1;
            while (userRepository.existsByUsername(username)) {
                username = base + suffix++;
            }
            return userRepository.save(new UserAccount(username, email, null));
        });

        return new OAuth2UserPrincipal(user, oAuth2User.getAttributes());
    }
}
