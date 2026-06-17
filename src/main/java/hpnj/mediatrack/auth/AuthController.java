package hpnj.mediatrack.auth;

import hpnj.mediatrack.domain.user.UserAccount;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserInfo> register(@Valid @RequestBody RegisterRequest req,
                                             HttpServletResponse response) {
        UserAccount user = authService.register(req);
        issueToken(response, user.getId());
        return ResponseEntity.ok(toUserInfo(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserInfo> login(@Valid @RequestBody LoginRequest req,
                                          HttpServletResponse response) {
        UserAccount user = authService.login(req);
        issueToken(response, user.getId());
        return ResponseEntity.ok(toUserInfo(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true).sameSite("Lax").path("/").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfo> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(toUserInfo(principal.account()));
    }

    private void issueToken(HttpServletResponse response, Long userId) {
        String token = jwtService.generate(userId);
        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true).sameSite("Lax").path("/").maxAge(7 * 24 * 60 * 60).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private UserInfo toUserInfo(UserAccount user) {
        return new UserInfo(user.getId(), user.getUsername(), user.getEmail());
    }
}
