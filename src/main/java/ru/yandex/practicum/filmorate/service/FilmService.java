package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
    GenreDbStorage genreStorage,
    MpaDbStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        validateMpaExists(film);
        validateGenresExist(film);
        Film addedFilm = filmStorage.addFilm(film);
        log.info("Added film: {}", addedFilm);
        return addedFilm;
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        validateMpaExists(film);
        validateGenresExist(film);
        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Updated film: {}", updatedFilm);
        return updatedFilm;
    }

    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        log.info("Returning all films: {}", films);
        return films;
    }

    public Film getFilmById(Long id) {
        Film film = filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
        log.info("Returning film: {}", film);
        return film;
    }

    public void addLike(Long filmId, Long userId) {
        getFilmById(filmId);
        getUserById(userId);
        filmStorage.addLike(filmId, userId);
        log.info("Added like to filmId={} by userId={}", filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        getFilmById(filmId);
        getUserById(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Removed like from filmId={} by userId={}", filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> popularFilms = filmStorage.getPopularFilms(count);
        log.info("Returning popular films: {}", popularFilms);
        return popularFilms;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше " + EARLIEST_RELEASE_DATE);
        }
    }

    private void validateMpaExists(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("MPA-рейтинг обязателен");
        }
        mpaStorage.getMpaById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден"));
    }

    private void validateGenresExist(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.getGenres().stream()
                    .map(Genre::getId)
                    .distinct()
                    .forEach(id -> genreStorage.getGenreById(id)
                            .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден")));
        }
    }

    private User getUserById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }
}