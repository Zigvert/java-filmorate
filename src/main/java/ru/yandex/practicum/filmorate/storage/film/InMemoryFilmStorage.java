package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long idCounter = 0;

    @Override
    public Film addFilm(Film film) {
        film.setId(++idCounter);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void deleteFilm(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        films.remove(id);
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId).orElseThrow(() ->
                new NotFoundException("Фильм с id=" + filmId + " не найден"));
        film.getLikes().add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId).orElseThrow(() ->
                new NotFoundException("Фильм с id=" + filmId + " не найден"));
        film.getLikes().remove(userId);
    }
}
