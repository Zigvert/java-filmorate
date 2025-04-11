package ru.yandex.practicum.filmorate.storageTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(FilmDbStorage.class)
@ComponentScan("ru.yandex.practicum.filmorate.storage")
class FilmDbStorageTests {
    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM film_likes");

        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                1L, "user1@example.com", "user1", "User One", LocalDate.of(1990, 1, 1)
        );

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = 1", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testAddFilm() {
        Film film = new Film(null, "Test Film", "Description", LocalDate.of(2020, 1, 1), 120, null,
                new Mpa(1L, "G"), new ArrayList<>());
        Film savedFilm = filmStorage.addFilm(film);

        assertThat(savedFilm).isNotNull();
        assertThat(savedFilm.getId()).isGreaterThan(0);
        assertThat(savedFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    void testAddFilmWithGenres() {
        List<Genre> genres = Arrays.asList(new Genre(1L, "Comedy"), new Genre(2L, "Drama"));
        Film film = new Film(null, "Test Film", "Description", LocalDate.of(2020, 1, 1), 120, null,
                new Mpa(1L, "G"), genres);
        Film savedFilm = filmStorage.addFilm(film);

        assertThat(savedFilm).isNotNull();
        assertThat(savedFilm.getId()).isGreaterThan(0);
        assertThat(savedFilm.getGenres())
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void testAddFilmWithNonExistentGenre() {
        List<Genre> genres = Arrays.asList(new Genre(999L, "NonExistent"));
        Film film = new Film(null, "Test Film", "Description", LocalDate.of(2020, 1, 1), 120, null,
                new Mpa(1L, "G"), genres);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> filmStorage.addFilm(film));
        assertThat(exception.getMessage()).contains("Жанры с id=[999]");
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film(null, "Test Film", "Description", LocalDate.of(2020, 1, 1), 120, null,
                new Mpa(1L, "G"), new ArrayList<>());
        Film savedFilm = filmStorage.addFilm(film);

        savedFilm.setName("Updated Film");
        Film updatedFilm = filmStorage.updateFilm(savedFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
    }

    @Test
    void testUpdateFilmWithGenres() {
        Film film = new Film(null, "Test Film", "Description", LocalDate.of(2020, 1, 1), 120, null,
                new Mpa(1L, "G"), new ArrayList<>());
        Film savedFilm = filmStorage.addFilm(film);

        List<Genre> genres = Arrays.asList(new Genre(1L, "Comedy"), new Genre(2L, "Drama"));
        savedFilm.setGenres(genres);
        Film updatedFilm = filmStorage.updateFilm(savedFilm);

        assertThat(updatedFilm.getGenres())
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void testUpdateFilmWithNonExistentGenre() {
        Film film = new Film(null, "Test Film", "Description", LocalDate.of(2020, 1, 1), 120, null,
                new Mpa(1L, "G"), new ArrayList<>());
        Film savedFilm = filmStorage.addFilm(film);

        List<Genre> genres = Arrays.asList(new Genre(999L, "NonExistent"));
        savedFilm.setGenres(genres);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> filmStorage.updateFilm(savedFilm));
        assertThat(exception.getMessage()).contains("Жанры с id=[999]");
    }

    @Test
    void testGetFilmById() {
        Film film = new Film(null, "Test Film", "Description", LocalDate.of(2020, 1, 1), 120, null,
                new Mpa(1L, "G"), new ArrayList<>());
        Film savedFilm = filmStorage.addFilm(film);

        Optional<Film> filmOptional = filmStorage.getFilmById(savedFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getId()).isEqualTo(savedFilm.getId()));
    }

    @Test
    void testGetAllFilms() {
        Film film1 = new Film(null, "Film1", "Desc1", LocalDate.of(2020, 1, 1), 120, null,
                new Mpa(1L, "G"), new ArrayList<>());
        Film film2 = new Film(null, "Film2", "Desc2", LocalDate.of(2021, 1, 1), 90, null,
                new Mpa(1L, "G"), new ArrayList<>());
        filmStorage.addFilm(film1);
        filmStorage.addFilm(film2);

        List<Film> films = filmStorage.getAllFilms();

        assertThat(films).hasSize(2);
    }

    @Test
    void testAddLikeAndGetLikes() {
        Film film = new Film(null, "Test Film", "Description", LocalDate.of(2020, 1, 1), 120, null,
                new Mpa(1L, "G"), new ArrayList<>());
        Film savedFilm = filmStorage.addFilm(film);

        filmStorage.addLike(savedFilm.getId(), 1L);
        Film filmWithLike = filmStorage.getFilmById(savedFilm.getId()).orElseThrow();

        assertThat(filmWithLike.getLikes()).contains(1L);
    }

    @Test
    void testRemoveLike() {
        Film film = new Film(null, "Test Film", "Description", LocalDate.of(2020, 1, 1), 120, null,
                new Mpa(1L, "G"), new ArrayList<>());
        Film savedFilm = filmStorage.addFilm(film);

        filmStorage.addLike(savedFilm.getId(), 1L);
        filmStorage.removeLike(savedFilm.getId(), 1L);
        Film filmWithoutLike = filmStorage.getFilmById(savedFilm.getId()).orElseThrow();

        assertThat(filmWithoutLike.getLikes()).isEmpty();
    }

    @Test
    void testDeleteFilm() {
        Film film = new Film(null, "Test Film", "Description", LocalDate.of(2020, 1, 1), 120, null,
                new Mpa(1L, "G"), new ArrayList<>());
        Film savedFilm = filmStorage.addFilm(film);

        filmStorage.deleteFilm(savedFilm.getId());
        Optional<Film> filmOptional = filmStorage.getFilmById(savedFilm.getId());

        assertThat(filmOptional).isEmpty();
    }
}