package ru.yandex.practicum.filmorate.modelTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
public class FilmTest {

    private Validator validator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        assertTrue(validator.validate(film).isEmpty(), "Валидный фильм не прошёл валидацию");
    }

    @Test
    public void testNullName() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertFalse(validator.validate(film).isEmpty(), "Фильм с null именем прошёл валидацию");
    }

    @Test
    public void testEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertFalse(validator.validate(film).isEmpty(), "Фильм с пустым именем прошёл валидацию");
    }

    @Test
    public void testDescriptionExact200Characters() {
        Film film = new Film();
        film.setName("Movie");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertTrue(validator.validate(film).isEmpty(), "Фильм с описанием ровно 200 символов не прошёл валидацию");
    }

    @Test
    public void testDescriptionTooLong() {
        Film film = new Film();
        film.setName("Movie");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertFalse(validator.validate(film).isEmpty(), "Фильм с описанием >200 символов прошёл валидацию");
    }

    @Test
    public void testNullDescription() {
        Film film = new Film();
        film.setName("Movie");
        film.setDescription(null);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertTrue(validator.validate(film).isEmpty(), "Фильм с null описанием не прошёл валидацию");
    }

    @Test
    public void testNullReleaseDate() {
        Film film = new Film();
        film.setName("Movie");
        film.setDescription("Description");
        film.setReleaseDate(null);
        film.setDuration(120);

        assertFalse(validator.validate(film).isEmpty(), "Фильм с null датой релиза прошёл валидацию");
    }

    @Test
    public void testFutureReleaseDate() {
        Film film = new Film();
        film.setName("Movie");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.now().plusDays(1));
        film.setDuration(120);

        assertFalse(validator.validate(film).isEmpty(), "Фильм с будущей датой релиза прошёл валидацию");
    }

    @Test
    public void testZeroDuration() {
        Film film = new Film();
        film.setName("Movie");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(0);

        assertFalse(validator.validate(film).isEmpty(), "Фильм с нулевой продолжительностью прошёл валидацию");
    }

    @Test
    public void testNegativeDuration() {
        Film film = new Film();
        film.setName("Movie");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(-10);

        assertFalse(validator.validate(film).isEmpty(), "Фильм с отрицательной продолжительностью прошёл валидацию");
    }

    @Test
    public void testAddFilmWithEmptyName() throws Exception {
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

    @Test
    public void testAddFilmWithNullReleaseDate() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setName("Movie");
        invalidFilm.setDescription("Description");
        invalidFilm.setReleaseDate(null);
        invalidFilm.setDuration(120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddFilmWithReleaseDateTooEarly() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setName("Movie");
        invalidFilm.setDescription("Description");
        invalidFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        invalidFilm.setDuration(120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddValidFilm() throws Exception {
        Film validFilm = new Film();
        validFilm.setName("Inception");
        validFilm.setDescription("A mind-bending thriller");
        validFilm.setReleaseDate(LocalDate.of(2010, 7, 16));
        validFilm.setDuration(148);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());
    }
}