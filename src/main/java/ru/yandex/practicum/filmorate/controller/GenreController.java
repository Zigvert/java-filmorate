package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {
    private static final Logger log = LoggerFactory.getLogger(GenreController.class);
    private final GenreDbStorage genreDbStorage;

    @GetMapping
    public List<Genre> getAllGenres() {
        List<Genre> genres = genreDbStorage.getAllGenres();
        log.info("Returning all genres: {}", genres);
        return genres;
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable Long id) {
        Genre genre = genreDbStorage.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
        log.info("Returning genre: {}", genre);
        return genre;
    }

    @PostMapping
    public Genre createGenre(@Valid @RequestBody Genre genre) {
        log.info("Creating genre: {}", genre);
        Genre createdGenre = genreDbStorage.createGenre(genre);
        log.info("Created genre: {}", createdGenre);
        return createdGenre;
    }

    @PutMapping
    public Genre updateGenre(@Valid @RequestBody Genre genre) {
        log.info("Updating genre: {}", genre);
        Genre updatedGenre = genreDbStorage.updateGenre(genre);
        log.info("Updated genre: {}", updatedGenre);
        return updatedGenre;
    }
}