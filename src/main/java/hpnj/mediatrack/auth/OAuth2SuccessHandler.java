package hpnj.mediatrack.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    public OAuth2SuccessHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generate(principal.account().getId());

        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true).sameSite("Lax").path("/").maxAge(7 * 24 * 60 * 60).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.sendRedirect("http://localhost:5173/media/all/");
    }
}
