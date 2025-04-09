package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GenreController {
    private static final Logger log = LoggerFactory.getLogger(GenreController.class);
    private final GenreDbStorage genreDbStorage;

    @GetMapping("/genres")
    public List<Genre> getAllGenres() {
        List<Genre> genres = genreDbStorage.getAllGenres();
        log.info("Returning all genres: {}", genres);
        return genres;
    }

    @GetMapping("/genres/{id}")
    public Genre getGenreById(@PathVariable Long id) {
        Genre genre = genreDbStorage.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
        log.info("Returning genre: {}", genre);
        return genre;
    }
}