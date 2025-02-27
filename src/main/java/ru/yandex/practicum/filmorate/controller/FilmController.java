package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private final List<Film> films = new ArrayList<>();

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Добавление фильма: {}", film);
        film.setId(films.size() + 1);
        films.add(film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Обновление фильма: {}", film);
        for (int i = 0; i < films.size(); i++) {
            if (films.get(i).getId() == film.getId()) {
                films.set(i, film);
                return film;
            }
        }
        log.warn("Фильм с id={} не найден", film.getId());
        throw new IllegalArgumentException("Фильм с id=" + film.getId() + " не найден");
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получение всех фильмов");
        return films;
    }
}