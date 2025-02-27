package ru.yandex.practicum.filmorate.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testAddFilmWithInvalidData() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setName("");
        invalidFilm.setDescription("Description");
        invalidFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        invalidFilm.setDuration(120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }
}