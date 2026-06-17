package hpnj.mediatrack.media;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    MediaService mediaService;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MediaController(mediaService)).build();
    }

    @Test
    void returnsAllMedia() throws Exception {
        given(mediaService.findAll()).willReturn(List.of(
                new MediaSummary(1L, "Inception", "Movie", LocalDate.of(2010, 7, 16)),
                new MediaSummary(2L, "Breaking Bad", "TVShow", null)
        ));

        mockMvc.perform(get("/api/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Inception"))
                .andExpect(jsonPath("$[0].type").value("Movie"))
                .andExpect(jsonPath("$[1].title").value("Breaking Bad"))
                .andExpect(jsonPath("$[1].releaseDate").doesNotExist());
    }

    @Test
    void returnsEmptyListWhenNoMedia() throws Exception {
        given(mediaService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
