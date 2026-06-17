package hpnj.mediatrack.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import hpnj.mediatrack.common.ConflictException;
import hpnj.mediatrack.common.UnauthorizedException;
import hpnj.mediatrack.config.GlobalExceptionHandler;
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
                .setControllerAdvice(new GlobalExceptionHandler())
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
