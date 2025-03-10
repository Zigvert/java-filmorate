package ru.yandex.practicum.filmorate.modelTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
public class FilmTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    @MockBean
    private FilmStorage filmStorage;

    @MockBean
    private UserStorage userStorage;

    private Film validFilm;

    @BeforeEach
    public void setUp() {
        validFilm = new Film();
        validFilm.setId(1L);
        validFilm.setName("Test Film");
        validFilm.setDescription("Test Description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);

        when(filmService.addFilm(any(Film.class))).thenReturn(validFilm);
        when(filmService.getFilmById(1L)).thenReturn(validFilm);
        when(filmService.getAllFilms()).thenReturn(Collections.singletonList(validFilm));
        when(filmService.getPopularFilms(10)).thenReturn(Collections.singletonList(validFilm));
        when(filmService.updateFilm(any(Film.class))).thenReturn(validFilm);
    }

    @Test
    public void testAddValidFilm() throws Exception {
        Film newFilm = new Film();
        newFilm.setName("New Film");
        newFilm.setDescription("New Description");
        newFilm.setReleaseDate(LocalDate.of(2010, 1, 1));
        newFilm.setDuration(90);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFilm)))
                .andExpect(status().isOk()) // POST возвращает 200 OK в твоём контроллере
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Film"));
    }

    @Test
    public void testAddFilmWithInvalidReleaseDate() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setName("Invalid Film");
        invalidFilm.setDescription("Invalid Description");
        invalidFilm.setReleaseDate(LocalDate.of(1800, 1, 1));
        invalidFilm.setDuration(90);

        when(filmService.addFilm(invalidFilm))
                .thenThrow(new ValidationException("Дата релиза не может быть раньше 1895-12-28"));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateFilm() throws Exception {
        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated Description");
        updatedFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        updatedFilm.setDuration(150);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Film"));
    }

    @Test
    public void testUpdateFilmWithInvalidId() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setId(999L);
        invalidFilm.setName("Invalid Film");
        invalidFilm.setDescription("Invalid Description");
        invalidFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidFilm.setDuration(90);

        when(filmService.updateFilm(invalidFilm))
                .thenThrow(new NotFoundException("Фильм с id=999 не найден"));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetFilmById() throws Exception {
        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Film"));
    }

    @Test
    public void testGetFilmByInvalidId() throws Exception {
        when(filmService.getFilmById(999L))
                .thenThrow(new NotFoundException("Фильм с id=999 не найден"));

        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllFilms() throws Exception {
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Film"));
    }

    @Test
    public void testAddLike() throws Exception {
        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());
        verify(filmService, times(1)).addLike(1L, 1L);
    }

    @Test
    public void testAddLikeWithInvalidUser() throws Exception {
        doThrow(new NotFoundException("Пользователь с ID 999 не найден"))
                .when(filmService).addLike(eq(1L), eq(999L));

        mockMvc.perform(put("/films/1/like/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRemoveLike() throws Exception {
        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isOk());
        verify(filmService, times(1)).removeLike(1L, 1L);
    }

    @Test
    public void testGetPopularFilms() throws Exception {
        mockMvc.perform(get("/films/popular?count=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Film"));
    }
}