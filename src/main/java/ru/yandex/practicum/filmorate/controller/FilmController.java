package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Добавление фильма: {}", film);
        if (film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше " + EARLIEST_RELEASE_DATE);
        }
        film.setId(films.size() + 1);
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Обновление фильма: {}", film);

        if (film.getId() <= 0 || !films.containsKey(film.getId())) {
            log.warn("Фильм с id={} не найден или id некорректен", film.getId());
            throw new IllegalArgumentException("Фильм с id=" + film.getId() + " не найден или id некорректен");
        }

        Film existingFilm = films.get(film.getId());

        if (film.getName() != null) {
            if (film.getName().isBlank()) {
                throw new ValidationException("Название фильма не может быть пустым");
            }
            existingFilm.setName(film.getName());
        }

        if (film.getDescription() != null) {
            if (film.getDescription().length() > 200) {
                throw new ValidationException("Описание не должно превышать 200 символов");
            }
            existingFilm.setDescription(film.getDescription());
        }

        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
                throw new ValidationException("Дата релиза не может быть раньше " + EARLIEST_RELEASE_DATE);
            }
            existingFilm.setReleaseDate(film.getReleaseDate());
        }

        if (film.getDuration() != 0) {
            if (film.getDuration() <= 0) {
                throw new ValidationException("Продолжительность фильма должна быть положительной");
            }
            existingFilm.setDuration(film.getDuration());
        }

        return existingFilm;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получение всех фильмов");
        return new ArrayList<>(films.values());
    }
}