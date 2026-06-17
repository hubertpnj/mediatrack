# Auth Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement email/password + Google OAuth2 registration and login with JWT stored in an HttpOnly SameSite=Lax cookie, plus a React frontend with `/login` and `/register` pages.

**Architecture:** Spring Security handles all auth logic — BCrypt for passwords, built-in OAuth2 client for Google, JJWT for JWT generation. A `JwtAuthFilter` reads the `auth_token` cookie on every request and populates `SecurityContext`. The Vite dev server proxies all traffic (including OAuth2 redirects) so cookies are consistently scoped to `localhost:5173`. The frontend uses TanStack Query v5 for data fetching and React Router v7 for routing.

**Tech Stack:** Spring Boot 4, Spring Security 6, JJWT 0.12.6, BCrypt, React 18, Vite 6, TypeScript, TanStack Query v5, React Router v7.

## Global Constraints

- Java 25, package root `hpnj.mediatrack`
- All backend tests use `@ExtendWith(MockitoExtension.class)` + `standaloneSetup` (follow existing `MediaControllerTest` pattern)
- JWT cookie name: `auth_token`, TTL: 7 days, `HttpOnly; SameSite=Lax; Path=/`
- Cookie domain: `localhost:5173` (all OAuth2 traffic routed through Vite proxy)
- All API paths prefixed `/api/`, OAuth2 paths `/oauth2/**` and `/login/oauth2/**`
- `app.jwt.secret` must be ≥ 32 bytes (256 bits)
- Google OAuth2 `redirect-uri` points to `http://localhost:5173/login/oauth2/code/google`
- Frontend: no `eslint` changes, no test framework changes, existing CSS variables only

---

## File Map

**Backend — new:**
- `src/main/java/hpnj/mediatrack/user/UserRepository.java`
- `src/main/java/hpnj/mediatrack/common/ConflictException.java`
- `src/main/java/hpnj/mediatrack/common/UnauthorizedException.java`
- `src/main/java/hpnj/mediatrack/auth/UserPrincipal.java`
- `src/main/java/hpnj/mediatrack/auth/OAuth2UserPrincipal.java`
- `src/main/java/hpnj/mediatrack/auth/JwtService.java`
- `src/main/java/hpnj/mediatrack/auth/JwtAuthFilter.java`
- `src/main/java/hpnj/mediatrack/auth/RegisterRequest.java`
- `src/main/java/hpnj/mediatrack/auth/LoginRequest.java`
- `src/main/java/hpnj/mediatrack/auth/UserInfo.java`
- `src/main/java/hpnj/mediatrack/auth/AuthService.java`
- `src/main/java/hpnj/mediatrack/auth/AuthController.java`
- `src/main/java/hpnj/mediatrack/auth/CustomOAuth2UserService.java`
- `src/main/java/hpnj/mediatrack/auth/OAuth2SuccessHandler.java`
- `src/main/java/hpnj/mediatrack/config/GlobalExceptionHandler.java`
- `src/main/resources/db/migration/V16__make_password_hash_nullable.sql`

**Backend — modified:**
- `src/main/java/hpnj/mediatrack/config/SecurityConfig.java`
- `src/main/resources/application.properties`
- `build.gradle`

**Backend — test:**
- `src/test/java/hpnj/mediatrack/auth/JwtServiceTest.java`
- `src/test/java/hpnj/mediatrack/auth/AuthControllerTest.java`

**Frontend — new:**
- `frontend/src/lib/api.ts`
- `frontend/src/context/AuthContext.tsx`
- `frontend/src/pages/CatalogPage.tsx`
- `frontend/src/pages/LoginPage.tsx`
- `frontend/src/pages/RegisterPage.tsx`

**Frontend — modified:**
- `frontend/package.json`
- `frontend/vite.config.ts`
- `frontend/src/main.tsx`
- `frontend/src/App.tsx`
- `frontend/src/hooks/useMedia.ts`
- `frontend/src/components/Header.tsx`
- `frontend/src/index.css`

---

## Task 1: DB migration + UserRepository + common exceptions + JwtService

**Files:**
- Create: `src/main/resources/db/migration/V16__make_password_hash_nullable.sql`
- Create: `src/main/java/hpnj/mediatrack/user/UserRepository.java`
- Create: `src/main/java/hpnj/mediatrack/common/ConflictException.java`
- Create: `src/main/java/hpnj/mediatrack/common/UnauthorizedException.java`
- Create: `src/main/java/hpnj/mediatrack/auth/JwtService.java`
- Modify: `src/main/resources/application.properties`
- Create: `src/test/java/hpnj/mediatrack/auth/JwtServiceTest.java`

**Interfaces:**
- Produces: `UserRepository` with `findByEmail`, `findByUsername`, `existsByEmail`, `existsByUsername`
- Produces: `JwtService.generate(Long userId): String`, `JwtService.extractUserId(String token): Long`
- Produces: `ConflictException(String message)`, `UnauthorizedException(String message)` (both extend `RuntimeException`)

- [ ] **Step 1: Generate JWT secret and add config to application.properties**

Run:
```bash
openssl rand -base64 32
```
Copy the output (e.g. `Xk3pZ9...`). Add to `src/main/resources/application.properties`:
```properties
# JWT
app.jwt.secret=PASTE_YOUR_GENERATED_SECRET_HERE

# Google OAuth2 (fill in after creating Google Cloud credentials)
spring.security.oauth2.client.registration.google.client-id=GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:5173/login/oauth2/code/google
```

- [ ] **Step 2: Write DB migration to make password_hash nullable**

`src/main/resources/db/migration/V16__make_password_hash_nullable.sql`:
```sql
ALTER TABLE user_account ALTER COLUMN password_hash DROP NOT NULL;
```

- [ ] **Step 3: Add oauth2-client to build.gradle**

Add after `spring-boot-starter-security`:
```groovy
implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
```

- [ ] **Step 4: Create UserRepository**

`src/main/java/hpnj/mediatrack/user/UserRepository.java`:
```java
package hpnj.mediatrack.user;

import hpnj.mediatrack.domain.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
```

- [ ] **Step 5: Create common exceptions**

`src/main/java/hpnj/mediatrack/common/ConflictException.java`:
```java
package hpnj.mediatrack.common;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
```

`src/main/java/hpnj/mediatrack/common/UnauthorizedException.java`:
```java
package hpnj.mediatrack.common;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) { super(message); }
}
```

- [ ] **Step 6: Write failing test for JwtService**

`src/test/java/hpnj/mediatrack/auth/JwtServiceTest.java`:
```java
package hpnj.mediatrack.auth;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long!!";
    private final JwtService jwtService = new JwtService(SECRET);

    @Test
    void roundtrip_userId_survives_generate_and_extract() {
        String token = jwtService.generate(42L);
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void extractUserId_throws_on_invalid_token() {
        assertThatThrownBy(() -> jwtService.extractUserId("not.a.token"))
                .isInstanceOf(Exception.class);
    }
}
```

- [ ] **Step 7: Run test — expect FAIL (JwtService doesn't exist yet)**

```bash
cd /Users/huya/code/java/mediatrack
./gradlew test --tests "hpnj.mediatrack.auth.JwtServiceTest" 2>&1 | tail -20
```
Expected: compilation error — `JwtService` not found.

- [ ] **Step 8: Create JwtService**

`src/main/java/hpnj/mediatrack/auth/JwtService.java`:
```java
package hpnj.mediatrack.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generate(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(7, ChronoUnit.DAYS)))
                .signWith(key)
                .compact();
    }

    public Long extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }
}
```

- [ ] **Step 9: Run test — expect PASS**

```bash
./gradlew test --tests "hpnj.mediatrack.auth.JwtServiceTest" 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`, both tests green.

- [ ] **Step 10: Commit**

```bash
git add build.gradle \
  src/main/resources/application.properties \
  src/main/resources/db/migration/V16__make_password_hash_nullable.sql \
  src/main/java/hpnj/mediatrack/user/UserRepository.java \
  src/main/java/hpnj/mediatrack/common/ \
  src/main/java/hpnj/mediatrack/auth/JwtService.java \
  src/test/java/hpnj/mediatrack/auth/JwtServiceTest.java
git commit -m "add UserRepository, JwtService, common exceptions, password_hash nullable migration"
```

---

## Task 2: UserPrincipal + JwtAuthFilter + SecurityConfig (stateless baseline)

**Files:**
- Create: `src/main/java/hpnj/mediatrack/auth/UserPrincipal.java`
- Create: `src/main/java/hpnj/mediatrack/auth/JwtAuthFilter.java`
- Modify: `src/main/java/hpnj/mediatrack/config/SecurityConfig.java`

**Interfaces:**
- Consumes: `JwtService.extractUserId(String)`, `UserRepository.findById(Long)`
- Produces: `UserPrincipal(UserAccount account)` implements `UserDetails`; `JwtAuthFilter` reads `auth_token` cookie and populates `SecurityContextHolder`

- [ ] **Step 1: Create UserPrincipal**

`src/main/java/hpnj/mediatrack/auth/UserPrincipal.java`:
```java
package hpnj.mediatrack.auth;

import hpnj.mediatrack.domain.user.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record UserPrincipal(UserAccount account) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() { return account.getPasswordHash(); }

    @Override
    public String getUsername() { return account.getEmail(); }
}
```

- [ ] **Step 2: Create JwtAuthFilter**

`src/main/java/hpnj/mediatrack/auth/JwtAuthFilter.java`:
```java
package hpnj.mediatrack.auth;

import hpnj.mediatrack.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null) {
            try {
                Long userId = jwtService.extractUserId(token);
                userRepository.findById(userId).ifPresent(user -> {
                    UserPrincipal principal = new UserPrincipal(user);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            } catch (Exception ignored) {}
        }
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "auth_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
```

- [ ] **Step 3: Replace SecurityConfig**

`src/main/java/hpnj/mediatrack/config/SecurityConfig.java`:
```java
package hpnj.mediatrack.config;

import hpnj.mediatrack.auth.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/media/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

- [ ] **Step 4: Verify app still starts and existing media endpoint works**

```bash
./gradlew bootRun 2>&1 | grep -E "Started|ERROR" | head -5
```
Then in a separate terminal:
```bash
curl -s http://localhost:8080/api/media | head -c 100
```
Expected: JSON array starting with `[{`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/hpnj/mediatrack/auth/UserPrincipal.java \
  src/main/java/hpnj/mediatrack/auth/JwtAuthFilter.java \
  src/main/java/hpnj/mediatrack/config/SecurityConfig.java
git commit -m "add JwtAuthFilter and stateless security config"
```

---

## Task 3: AuthService + AuthController (register, login, logout, me)

**Files:**
- Create: `src/main/java/hpnj/mediatrack/auth/RegisterRequest.java`
- Create: `src/main/java/hpnj/mediatrack/auth/LoginRequest.java`
- Create: `src/main/java/hpnj/mediatrack/auth/UserInfo.java`
- Create: `src/main/java/hpnj/mediatrack/auth/AuthService.java`
- Create: `src/main/java/hpnj/mediatrack/auth/AuthController.java`
- Create: `src/test/java/hpnj/mediatrack/auth/AuthControllerTest.java`

**Interfaces:**
- Consumes: `UserRepository`, `JwtService`, `PasswordEncoder` (from SecurityConfig), `ConflictException`, `UnauthorizedException`
- Produces: `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me` — all return `UserInfo` or 401

- [ ] **Step 1: Create DTOs**

`src/main/java/hpnj/mediatrack/auth/RegisterRequest.java`:
```java
package hpnj.mediatrack.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password
) {}
```

`src/main/java/hpnj/mediatrack/auth/LoginRequest.java`:
```java
package hpnj.mediatrack.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}
```

`src/main/java/hpnj/mediatrack/auth/UserInfo.java`:
```java
package hpnj.mediatrack.auth;

public record UserInfo(Long id, String username, String email) {}
```

- [ ] **Step 2: Write failing tests for AuthController**

`src/test/java/hpnj/mediatrack/auth/AuthControllerTest.java`:
```java
package hpnj.mediatrack.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import hpnj.mediatrack.common.ConflictException;
import hpnj.mediatrack.common.UnauthorizedException;
import hpnj.mediatrack.domain.user.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthService authService;
    @Mock JwtService jwtService;

    MockMvc mockMvc;
    ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService, jwtService))
                .build();
    }

    @Test
    void register_returns_200_with_user_info() throws Exception {
        UserAccount user = new UserAccount("alice", "alice@example.com", "hash");
        given(authService.register(any())).willReturn(user);
        given(jwtService.generate(user.getId())).willReturn("tok");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new RegisterRequest("alice", "alice@example.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void register_returns_409_on_conflict() throws Exception {
        given(authService.register(any())).willThrow(new ConflictException("Email already registered"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        new RegisterRequest("alice", "alice@example.com", "password123"))))
                .andExpect(status().isConflict());
    }

    @Test
    void login_returns_200_with_user_info() throws Exception {
        UserAccount user = new UserAccount("alice", "alice@example.com", "hash");
        given(authService.login(any())).willReturn(user);
        given(jwtService.generate(user.getId())).willReturn("tok");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LoginRequest("alice@example.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void login_returns_401_on_bad_credentials() throws Exception {
        given(authService.login(any())).willThrow(new UnauthorizedException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LoginRequest("alice@example.com", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_returns_200() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());
    }

    @Test
    void me_returns_401_when_not_authenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
```

- [ ] **Step 3: Run tests — expect FAIL**

```bash
./gradlew test --tests "hpnj.mediatrack.auth.AuthControllerTest" 2>&1 | tail -15
```
Expected: compilation error — `AuthController`, `AuthService` not found.

- [ ] **Step 4: Create AuthService**

`src/main/java/hpnj/mediatrack/auth/AuthService.java`:
```java
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
```

- [ ] **Step 5: Create AuthController**

`src/main/java/hpnj/mediatrack/auth/AuthController.java`:
```java
package hpnj.mediatrack.auth;

import hpnj.mediatrack.common.ConflictException;
import hpnj.mediatrack.common.UnauthorizedException;
import hpnj.mediatrack.domain.user.UserAccount;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<UserInfo> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
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
```

Note: `register_returns_409_on_conflict` and `login_returns_401_on_bad_credentials` tests expect Spring MVC to propagate the exceptions as 409/401. These require the `GlobalExceptionHandler` (Task 4). For now the tests for conflict/unauthorized will return 500. Add `@ExceptionHandler` inline if needed, or implement Task 4 first and re-run.

- [ ] **Step 6: Run tests — expect PASS (except conflict/unauthorized tests need Task 4)**

```bash
./gradlew test --tests "hpnj.mediatrack.auth.AuthControllerTest" 2>&1 | tail -20
```
Expected: `register_returns_200`, `login_returns_200`, `logout_returns_200`, `me_returns_401` pass. The 409/401 exception tests will pass after Task 4.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/hpnj/mediatrack/auth/RegisterRequest.java \
  src/main/java/hpnj/mediatrack/auth/LoginRequest.java \
  src/main/java/hpnj/mediatrack/auth/UserInfo.java \
  src/main/java/hpnj/mediatrack/auth/AuthService.java \
  src/main/java/hpnj/mediatrack/auth/AuthController.java \
  src/test/java/hpnj/mediatrack/auth/AuthControllerTest.java
git commit -m "add AuthService and AuthController with register/login/logout/me"
```

---

## Task 4: GlobalExceptionHandler

**Files:**
- Create: `src/main/java/hpnj/mediatrack/config/GlobalExceptionHandler.java`

**Interfaces:**
- Consumes: `ConflictException`, `UnauthorizedException` from `hpnj.mediatrack.common`
- Produces: JSON `{ "error": "..." }` with correct HTTP status for all exception types

- [ ] **Step 1: Create GlobalExceptionHandler**

`src/main/java/hpnj/mediatrack/config/GlobalExceptionHandler.java`:
```java
package hpnj.mediatrack.config;

import hpnj.mediatrack.common.ConflictException;
import hpnj.mediatrack.common.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
```

- [ ] **Step 2: Run all auth tests — expect all PASS**

```bash
./gradlew test --tests "hpnj.mediatrack.auth.*" 2>&1 | tail -15
```
Expected: all 7 tests in `AuthControllerTest` + 2 in `JwtServiceTest` green.

- [ ] **Step 3: Smoke test register endpoint manually**

Start the app (`./gradlew bootRun` in background), then:
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"secret123"}' \
  -c /tmp/cookies.txt -v 2>&1 | grep -E "Set-Cookie|{|}"
```
Expected: `Set-Cookie: auth_token=...; HttpOnly` and `{"id":1,"username":"alice","email":"alice@example.com"}`.

- [ ] **Step 4: Smoke test me endpoint**

```bash
curl -s http://localhost:8080/api/auth/me -b /tmp/cookies.txt
```
Expected: `{"id":1,"username":"alice","email":"alice@example.com"}`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/hpnj/mediatrack/config/GlobalExceptionHandler.java
git commit -m "add global exception handler for consistent JSON error responses"
```

---

## Task 5: Google OAuth2 (CustomOAuth2UserService + OAuth2UserPrincipal + OAuth2SuccessHandler + SecurityConfig update)

**Files:**
- Create: `src/main/java/hpnj/mediatrack/auth/OAuth2UserPrincipal.java`
- Create: `src/main/java/hpnj/mediatrack/auth/CustomOAuth2UserService.java`
- Create: `src/main/java/hpnj/mediatrack/auth/OAuth2SuccessHandler.java`
- Modify: `src/main/java/hpnj/mediatrack/config/SecurityConfig.java`

**Interfaces:**
- Consumes: `UserRepository`, `JwtService`
- Produces: After Google auth, `auth_token` cookie set, redirect to `http://localhost:5173/media/all/`

- [ ] **Step 1: Create OAuth2UserPrincipal**

`src/main/java/hpnj/mediatrack/auth/OAuth2UserPrincipal.java`:
```java
package hpnj.mediatrack.auth;

import hpnj.mediatrack.domain.user.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public record OAuth2UserPrincipal(UserAccount account, Map<String, Object> attributes)
        implements OAuth2User {

    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() { return account.getEmail(); }
}
```

- [ ] **Step 2: Create CustomOAuth2UserService**

`src/main/java/hpnj/mediatrack/auth/CustomOAuth2UserService.java`:
```java
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
```

- [ ] **Step 3: Create OAuth2SuccessHandler**

`src/main/java/hpnj/mediatrack/auth/OAuth2SuccessHandler.java`:
```java
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
```

- [ ] **Step 4: Update SecurityConfig to wire OAuth2**

Replace the `filterChain` bean in `SecurityConfig.java`:
```java
private final JwtAuthFilter jwtAuthFilter;
private final CustomOAuth2UserService oAuth2UserService;
private final OAuth2SuccessHandler oAuth2SuccessHandler;

public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                      CustomOAuth2UserService oAuth2UserService,
                      OAuth2SuccessHandler oAuth2SuccessHandler) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.oAuth2UserService = oAuth2UserService;
    this.oAuth2SuccessHandler = oAuth2SuccessHandler;
}

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/media/**").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(ui -> ui.userService(oAuth2UserService))
            .successHandler(oAuth2SuccessHandler)
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

- [ ] **Step 5: Run full test suite — expect green**

```bash
./gradlew test 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/hpnj/mediatrack/auth/OAuth2UserPrincipal.java \
  src/main/java/hpnj/mediatrack/auth/CustomOAuth2UserService.java \
  src/main/java/hpnj/mediatrack/auth/OAuth2SuccessHandler.java \
  src/main/java/hpnj/mediatrack/config/SecurityConfig.java
git commit -m "add Google OAuth2 login with user auto-provisioning"
```

---

## Task 6: Frontend — dependencies + api helper + Vite proxy + useMedia migration

**Files:**
- Modify: `frontend/package.json` (add deps)
- Modify: `frontend/vite.config.ts` (proxy + base)
- Create: `frontend/src/lib/api.ts`
- Modify: `frontend/src/hooks/useMedia.ts`

**Interfaces:**
- Produces: `apiFetch<T>(path, init?): Promise<T>` — wraps fetch with `credentials: 'include'`
- Produces: `useMedia()` returning TanStack Query result with `status: 'pending' | 'error' | 'success'`, `data: MediaSummary[]`

- [ ] **Step 1: Install frontend dependencies**

```bash
cd /Users/huya/code/java/mediatrack/frontend
npm install react-router-dom@7 @tanstack/react-query@5
```
Expected: packages added to `package.json`, no peer dep errors.

- [ ] **Step 2: Update vite.config.ts — proxy OAuth2 routes + reset base to /**

`frontend/vite.config.ts`:
```ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  base: '/',
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/oauth2': 'http://localhost:8080',
      '/login/oauth2': 'http://localhost:8080',
    }
  }
})
```

Note: `base` changes from `/media/all/` to `/`. Images in `public/images/` are now at `/images/1.jpg`. Update `MediaCard.tsx` image src in Task 9.

- [ ] **Step 3: Create api helper**

`frontend/src/lib/api.ts`:
```ts
export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, {
    ...init,
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    throw new Error((body as { error?: string }).error ?? res.statusText)
  }
  return res.json() as Promise<T>
}
```

- [ ] **Step 4: Migrate useMedia to TanStack Query**

`frontend/src/hooks/useMedia.ts`:
```ts
import { useQuery } from '@tanstack/react-query'
import type { MediaSummary } from '../types'
import { apiFetch } from '../lib/api'

export function useMedia() {
  return useQuery<MediaSummary[]>({
    queryKey: ['media'],
    queryFn: () => apiFetch<MediaSummary[]>('/api/media'),
  })
}
```

- [ ] **Step 5: Verify Vite starts and media loads**

```bash
cd /Users/huya/code/java/mediatrack/frontend
npm run dev 2>&1 | head -10
```
Open `http://localhost:5173/` — media cards should render (Spring Boot must be running).

- [ ] **Step 6: Commit**

```bash
cd /Users/huya/code/java/mediatrack
git add frontend/package.json frontend/package-lock.json \
  frontend/vite.config.ts \
  frontend/src/lib/api.ts \
  frontend/src/hooks/useMedia.ts
git commit -m "add TanStack Query, React Router deps, api helper, migrate useMedia"
```

---

## Task 7: AuthContext + React providers in main.tsx

**Files:**
- Create: `frontend/src/context/AuthContext.tsx`
- Modify: `frontend/src/main.tsx`

**Interfaces:**
- Produces: `useAuth()` hook returning `{ user: UserInfo | null, isLoading: boolean, login(req): Promise<void>, register(req): Promise<void>, logout(): Promise<void> }`
- `UserInfo`: `{ id: number, username: string, email: string }`

- [ ] **Step 1: Add UserInfo type to frontend/src/types.ts**

Open `frontend/src/types.ts` and add at the bottom:
```ts
export interface UserInfo {
  id: number
  username: string
  email: string
}
```

- [ ] **Step 2: Create AuthContext**

`frontend/src/context/AuthContext.tsx`:
```tsx
import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import type { UserInfo } from '../types'
import { apiFetch } from '../lib/api'

interface AuthContextValue {
  user: UserInfo | null
  isLoading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (username: string, email: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    apiFetch<UserInfo>('/api/auth/me')
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setIsLoading(false))
  }, [])

  async function login(email: string, password: string) {
    const u = await apiFetch<UserInfo>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    })
    setUser(u)
  }

  async function register(username: string, email: string, password: string) {
    const u = await apiFetch<UserInfo>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, email, password }),
    })
    setUser(u)
  }

  async function logout() {
    await apiFetch('/api/auth/logout', { method: 'POST' })
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
```

- [ ] **Step 3: Update main.tsx with all providers**

`frontend/src/main.tsx`:
```tsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider } from './context/AuthContext'
import './index.css'
import App from './App.tsx'

const queryClient = new QueryClient()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <App />
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
)
```

- [ ] **Step 4: Verify no runtime errors**

Open `http://localhost:5173/` — should still render the catalog. Check browser console for errors.

- [ ] **Step 5: Commit**

```bash
cd /Users/huya/code/java/mediatrack
git add frontend/src/types.ts \
  frontend/src/context/AuthContext.tsx \
  frontend/src/main.tsx
git commit -m "add AuthContext with login/register/logout, wrap app in providers"
```

---

## Task 8: React Router routes + CatalogPage

**Files:**
- Create: `frontend/src/pages/CatalogPage.tsx`
- Modify: `frontend/src/App.tsx`

**Interfaces:**
- Consumes: `useMedia`, `useAuth`, `Header`, `TypeFilter`, `MediaGrid` (all existing)
- Produces: route `/media/all` renders catalog; `/` redirects to `/media/all`

- [ ] **Step 1: Extract CatalogPage from App.tsx**

`frontend/src/pages/CatalogPage.tsx`:
```tsx
import { useState, useMemo } from 'react'
import { useMedia } from '../hooks/useMedia'
import { useAuth } from '../context/AuthContext'
import type { MediaType } from '../types'
import Header from '../components/Header'
import TypeFilter from '../components/TypeFilter'
import MediaGrid from '../components/MediaGrid'

export default function CatalogPage() {
  const { data, status } = useMedia()
  const auth = useAuth()
  const [search, setSearch] = useState('')
  const [activeType, setActiveType] = useState<MediaType | null>(null)

  const allItems = status === 'success' ? data : []

  const typeCounts = useMemo(() => {
    const counts: Partial<Record<MediaType, number>> = {}
    for (const item of allItems) {
      counts[item.type] = (counts[item.type] ?? 0) + 1
    }
    return counts
  }, [allItems])

  const filtered = useMemo(() => {
    let items = allItems
    if (activeType) items = items.filter(m => m.type === activeType)
    const q = search.trim().toLowerCase()
    if (q) items = items.filter(m => m.title.toLowerCase().includes(q))
    return items
  }, [allItems, activeType, search])

  return (
    <div className="app">
      <Header
        search={search}
        onSearch={setSearch}
        totalCount={allItems.length}
        filteredCount={filtered.length}
        theme={auth.theme ?? 'light'}
        onToggleTheme={auth.toggleTheme ?? (() => {})}
      />
      <main className="main">
        <TypeFilter
          activeType={activeType}
          onChange={setActiveType}
          counts={typeCounts}
        />
        <MediaGrid
          status={status}
          items={filtered}
          isFiltered={activeType !== null || search.trim() !== ''}
        />
      </main>
    </div>
  )
}
```

Note: `auth.theme` and `auth.toggleTheme` don't exist on `AuthContext` yet — theme state currently lives in `App.tsx`. Move theme state to `CatalogPage` directly (it doesn't need to be global):

`frontend/src/pages/CatalogPage.tsx` — replace the Header props for theme:
```tsx
import { useState, useMemo, useEffect } from 'react'
// ... other imports

export default function CatalogPage() {
  // ... existing state
  const [theme, setTheme] = useState<'light' | 'dark'>(() => {
    const stored = localStorage.getItem('theme')
    if (stored === 'light' || stored === 'dark') return stored
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  })

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('theme', theme)
  }, [theme])

  function toggleTheme() { setTheme(t => t === 'light' ? 'dark' : 'light') }

  // ... rest of component, pass theme/toggleTheme to Header
}
```

- [ ] **Step 2: Replace App.tsx with router**

`frontend/src/App.tsx`:
```tsx
import { Routes, Route, Navigate } from 'react-router-dom'
import CatalogPage from './pages/CatalogPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'

export default function App() {
  return (
    <Routes>
      <Route path="/media/all" element={<CatalogPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="*" element={<Navigate to="/media/all" replace />} />
    </Routes>
  )
}
```

Create stub pages so the app compiles:

`frontend/src/pages/LoginPage.tsx` (stub):
```tsx
export default function LoginPage() {
  return <div>Login</div>
}
```

`frontend/src/pages/RegisterPage.tsx` (stub):
```tsx
export default function RegisterPage() {
  return <div>Register</div>
}
```

- [ ] **Step 3: Update MediaCard image src (base changed from /media/all/ to /)**

In `frontend/src/components/MediaCard.tsx`, change:
```tsx
src={`${import.meta.env.BASE_URL}images/${item.id}.jpg`}
```
to:
```tsx
src={`/images/${item.id}.jpg`}
```

- [ ] **Step 4: Verify catalog at /media/all and redirect from /**

Open `http://localhost:5173/` — should redirect to `http://localhost:5173/media/all`.
Open `http://localhost:5173/login` — should show "Login".
Images should load correctly.

- [ ] **Step 5: Commit**

```bash
cd /Users/huya/code/java/mediatrack
git add frontend/src/App.tsx \
  frontend/src/pages/CatalogPage.tsx \
  frontend/src/pages/LoginPage.tsx \
  frontend/src/pages/RegisterPage.tsx \
  frontend/src/components/MediaCard.tsx
git commit -m "add React Router, CatalogPage, stub Login/Register routes"
```

---

## Task 9: LoginPage + RegisterPage + Header auth buttons

**Files:**
- Modify: `frontend/src/pages/LoginPage.tsx`
- Modify: `frontend/src/pages/RegisterPage.tsx`
- Modify: `frontend/src/components/Header.tsx`
- Modify: `frontend/src/index.css`

**Interfaces:**
- Consumes: `useAuth()` for `login`, `register`, `logout`, `user`
- Produces: `/login` and `/register` pages with email/password forms + Google button; Header shows auth state

- [ ] **Step 1: Build LoginPage**

`frontend/src/pages/LoginPage.tsx`:
```tsx
import { useState, FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function LoginPage() {
  const { login, user } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (user) {
    navigate('/media/all', { replace: true })
    return null
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email, password)
      navigate('/media/all', { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">
          <div className="logo-mark">M</div>
          <span className="logo-text">Media<span>Track</span></span>
        </div>
        <h1 className="auth-title">Welcome back</h1>

        <form className="auth-form" onSubmit={handleSubmit}>
          {error && <p className="auth-error">{error}</p>}
          <label className="auth-label">
            Email
            <input className="auth-input" type="email" value={email}
              onChange={e => setEmail(e.target.value)} required autoFocus />
          </label>
          <label className="auth-label">
            Password
            <input className="auth-input" type="password" value={password}
              onChange={e => setPassword(e.target.value)} required />
          </label>
          <button className="auth-btn auth-btn--primary" type="submit" disabled={loading}>
            {loading ? 'Logging in…' : 'Log in'}
          </button>
        </form>

        <div className="auth-divider"><span>or</span></div>

        <a className="auth-btn auth-btn--google" href="/oauth2/authorization/google">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
          </svg>
          Sign in with Google
        </a>

        <p className="auth-footer">
          Don't have an account? <Link to="/register">Sign up</Link>
        </p>
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Build RegisterPage**

`frontend/src/pages/RegisterPage.tsx`:
```tsx
import { useState, FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function RegisterPage() {
  const { register, user } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (user) {
    navigate('/media/all', { replace: true })
    return null
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(username, email, password)
      navigate('/media/all', { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">
          <div className="logo-mark">M</div>
          <span className="logo-text">Media<span>Track</span></span>
        </div>
        <h1 className="auth-title">Create account</h1>

        <form className="auth-form" onSubmit={handleSubmit}>
          {error && <p className="auth-error">{error}</p>}
          <label className="auth-label">
            Username
            <input className="auth-input" type="text" value={username}
              onChange={e => setUsername(e.target.value)} required autoFocus
              minLength={3} maxLength={50} />
          </label>
          <label className="auth-label">
            Email
            <input className="auth-input" type="email" value={email}
              onChange={e => setEmail(e.target.value)} required />
          </label>
          <label className="auth-label">
            Password
            <input className="auth-input" type="password" value={password}
              onChange={e => setPassword(e.target.value)} required minLength={8} />
          </label>
          <button className="auth-btn auth-btn--primary" type="submit" disabled={loading}>
            {loading ? 'Creating account…' : 'Sign up'}
          </button>
        </form>

        <div className="auth-divider"><span>or</span></div>

        <a className="auth-btn auth-btn--google" href="/oauth2/authorization/google">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
          </svg>
          Sign up with Google
        </a>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  )
}
```

- [ ] **Step 3: Update Header with auth buttons**

Replace the Header interface and component in `frontend/src/components/Header.tsx`:
```tsx
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

interface Props {
  search: string
  onSearch: (value: string) => void
  totalCount: number
  filteredCount: number
  theme: 'light' | 'dark'
  onToggleTheme: () => void
}

export default function Header({ search, onSearch, totalCount, filteredCount, theme, onToggleTheme }: Props) {
  const { user, logout } = useAuth()
  const showCount = totalCount > 0

  return (
    <header className="header">
      <div className="header__inner">
        <div className="header__logo">
          <div className="logo-mark">M</div>
          <span className="logo-text">Media<span>Track</span></span>
          {showCount && (
            <span className="header__count">
              {filteredCount === totalCount
                ? `${totalCount} titles`
                : `${filteredCount} of ${totalCount}`}
            </span>
          )}
        </div>

        <div className="search-wrapper">
          <svg className="search-icon" viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="1.8">
            <circle cx="8.5" cy="8.5" r="5.5" />
            <path d="M15 15l-3-3" strokeLinecap="round" />
          </svg>
          <input
            className="search-input"
            type="search"
            placeholder="Search titles..."
            value={search}
            onChange={e => onSearch(e.target.value)}
            aria-label="Search media by title"
          />
        </div>

        <div className="header__actions">
          <button className="theme-toggle" onClick={onToggleTheme} aria-label="Toggle dark mode">
            {theme === 'dark' ? (
              <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
                <path d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4.22 1.78a1 1 0 010 1.42l-.7.7a1 1 0 11-1.42-1.42l.7-.7a1 1 0 011.42 0zM18 9a1 1 0 110 2h-1a1 1 0 110-2h1zM5.78 14.22a1 1 0 010 1.42l-.7.7a1 1 0 11-1.42-1.42l.7-.7a1 1 0 011.42 0zM10 16a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zm-6-7a1 1 0 110 2H3a1 1 0 110-2h1zm1.78-5.22a1 1 0 011.42 0l.7.7a1 1 0 01-1.42 1.42l-.7-.7a1 1 0 010-1.42zM10 6a4 4 0 100 8 4 4 0 000-8z"/>
              </svg>
            ) : (
              <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
                <path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z"/>
              </svg>
            )}
          </button>

          {user ? (
            <div className="header__user">
              <span className="header__username">{user.username}</span>
              <button className="auth-link-btn" onClick={() => logout()}>Log out</button>
            </div>
          ) : (
            <div className="header__user">
              <Link to="/login" className="auth-link-btn">Log in</Link>
              <Link to="/register" className="auth-btn-pill">Sign up</Link>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}
```

- [ ] **Step 4: Add auth styles to index.css**

Append to `frontend/src/index.css`:
```css
/* ── Header auth actions ────────────────────────────────── */
.header__actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
  margin-left: auto;
}

.search-wrapper {
  margin-left: 0;
}

.header__user {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.header__username {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--text-2);
}

.auth-link-btn {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--text-2);
  text-decoration: none;
  padding: 0.3rem 0.6rem;
  border-radius: var(--radius-md);
  transition: color var(--dur) var(--ease);
  background: none;
  border: none;
  cursor: pointer;
}

.auth-link-btn:hover { color: var(--accent); }

.auth-btn-pill {
  font-size: 0.82rem;
  font-weight: 700;
  color: #fff;
  text-decoration: none;
  background: var(--accent);
  padding: 0.35rem 0.85rem;
  border-radius: var(--radius-full);
  transition: opacity var(--dur) var(--ease);
}

.auth-btn-pill:hover { opacity: 0.88; }

/* ── Auth pages ─────────────────────────────────────────── */
.auth-page {
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  padding: 1.5rem;
}

.auth-card {
  width: 100%;
  max-width: 380px;
  background: var(--card);
  border: 1.5px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.auth-logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.auth-title {
  font-size: 1.4rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--text-1);
  margin: 0;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 0.875rem;
}

.auth-label {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--text-2);
}

.auth-input {
  padding: 0.55rem 0.75rem;
  background: var(--surface);
  border: 1.5px solid var(--border);
  border-radius: var(--radius-md);
  font-size: 0.9rem;
  color: var(--text-1);
  outline: none;
  transition: border-color var(--dur) var(--ease), box-shadow var(--dur) var(--ease);
}

.auth-input:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-soft);
}

.auth-error {
  font-size: 0.82rem;
  color: #dc2626;
  background: rgba(220,38,38,.08);
  border: 1px solid rgba(220,38,38,.2);
  border-radius: var(--radius-md);
  padding: 0.5rem 0.75rem;
  margin: 0;
}

.auth-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.6rem 1rem;
  border-radius: var(--radius-md);
  font-size: 0.9rem;
  font-weight: 600;
  border: none;
  cursor: pointer;
  text-decoration: none;
  transition: opacity var(--dur) var(--ease);
}

.auth-btn:hover { opacity: 0.88; }
.auth-btn:disabled { opacity: 0.55; cursor: not-allowed; }

.auth-btn--primary {
  background: var(--accent);
  color: #fff;
}

.auth-btn--google {
  background: var(--surface);
  border: 1.5px solid var(--border);
  color: var(--text-1);
}

.auth-divider {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: var(--text-3);
  font-size: 0.78rem;
}

.auth-divider::before,
.auth-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border);
}

.auth-footer {
  font-size: 0.82rem;
  color: var(--text-2);
  text-align: center;
  margin: 0;
}

.auth-footer a {
  color: var(--accent);
  font-weight: 600;
  text-decoration: none;
}
```

- [ ] **Step 5: Fix search-wrapper margin-left conflict**

In `index.css`, find `.search-wrapper` and change `margin-left: auto` to `margin-left: 0` (the auto margin is now on `.header__actions`):
```css
.search-wrapper {
  position: relative;
  flex: 1;
  max-width: 360px;
  margin-left: 0;
}
```

- [ ] **Step 6: Verify full flow**

1. Open `http://localhost:5173/media/all` — Header shows "Log in" + "Sign up" buttons
2. Click "Sign up" → `/register` page renders
3. Register with username/email/password → redirects to catalog, Header shows username + "Log out"
4. Click "Log out" → Header shows auth buttons again
5. Click "Log in" → `/login` page, login with same credentials → catalog with username

- [ ] **Step 7: Commit**

```bash
cd /Users/huya/code/java/mediatrack
git add frontend/src/pages/LoginPage.tsx \
  frontend/src/pages/RegisterPage.tsx \
  frontend/src/components/Header.tsx \
  frontend/src/index.css
git commit -m "add login and register pages, header auth buttons"
```

---

## Self-Review

**Spec coverage:**
- ✅ `POST /api/auth/register` — Task 3
- ✅ `POST /api/auth/login` — Task 3
- ✅ `POST /api/auth/logout` — Task 3
- ✅ `GET /api/auth/me` — Task 3
- ✅ Google OAuth2 — Task 5
- ✅ JWT in HttpOnly SameSite=Lax cookie — Tasks 3, 5
- ✅ BCrypt passwords — Task 2 (SecurityConfig `PasswordEncoder` bean)
- ✅ `user_account.password_hash` nullable — Task 1 (V16 migration)
- ✅ Public: `GET /api/media/**` — Task 2
- ✅ `GlobalExceptionHandler` — Task 4
- ✅ React Router v7 — Task 6
- ✅ TanStack Query v5 — Task 6
- ✅ `/login` and `/register` routes — Tasks 8, 9
- ✅ Header auth buttons (top-right) — Task 9
- ✅ Vite proxy for OAuth2 routes — Task 6
- ✅ `package-by-feature` backend structure — all tasks follow `auth/`, `user/`, `config/` packages

**Type consistency:** `UserInfo(Long id, String username, String email)` used in `AuthController.toUserInfo()` and as response type — consistent. `UserPrincipal.account()` used in `AuthController.me()` and `OAuth2SuccessHandler` — consistent.

**No placeholders:** all steps have complete code.
