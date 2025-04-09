package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Primary
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        log.info("Adding new film: {}", film.getName());
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);

        updateGenres(film);
        film.setLikes(new HashSet<>());
        log.info("Film added with id {}: {}", filmId, film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Updating film with id {}: {}", film.getId(), film.getName());
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rowsAffected == 0) {
            log.warn("Film with id {} not found", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        updateGenres(film);
        updateLikes(film);
        return getFilmById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + film.getId() + " не найден после обновления"));
    }

    @Override
    public void deleteFilm(Long id) {
        log.info("Deleting film with id {}", id);
        String sql = "DELETE FROM films WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            log.warn("Film with id {} not found", id);
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        log.info("Film with id {} deleted", id);
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        log.info("Retrieving film with id {}", id);
        String sql = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "WHERE f.id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
            if (film != null) {
                film.setGenres(getGenresByFilmId(id));
                film.setLikes(new HashSet<>(getLikesByFilmId(id)));
                log.info("Found film with id {}: {}", id, film);
                return Optional.of(film);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.info("Film with id {} not found or error occurred: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAllFilms() {
        log.info("Retrieving all films");
        String sql = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        for (Film film : films) {
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setLikes(new HashSet<>(getLikesByFilmId(film.getId())));
        }
        log.info("Retrieved {} films", films.size());
        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.info("Adding like to film {} by user {}", filmId, userId);
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, filmId, userId);
            log.info("Like added to film {} by user {}", filmId, userId);
        } catch (Exception e) {
            log.warn("Failed to add like to film {} by user {}: {}", filmId, userId, e.getMessage());
            throw new NotFoundException("Не удалось добавить лайк: фильм или пользователь не найден");
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        log.info("Removing like from film {} by user {}", filmId, userId);
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, filmId, userId);
        if (rowsAffected == 0) {
            log.warn("Like not found for film {} by user {}", filmId, userId);
        } else {
            log.info("Like removed from film {} by user {}", filmId, userId);
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        log.info("Retrieving top {} popular films", count);
        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) AS like_count " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                "ORDER BY like_count DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);
        for (Film film : films) {
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setLikes(new HashSet<>(getLikesByFilmId(film.getId())));
        }
        log.info("Retrieved {} popular films", films.size());
        return films;
    }

    private void updateGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            Set<Long> uniqueGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .filter(genreId -> {
                        Integer count = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM genres WHERE id = ?", Integer.class, genreId);
                        return count != null && count > 0;
                    })
                    .collect(Collectors.toSet());
            for (Long genreId : uniqueGenreIds) {
                try {
                    jdbcTemplate.update(genreSql, film.getId(), genreId);
                } catch (Exception e) {
                    log.warn("Failed to add genre {} to film {}: {}", genreId, film.getId(), e.getMessage());
                }
            }
            film.setGenres(getGenresByFilmId(film.getId()));
        } else {
            film.setGenres(new ArrayList<>());
        }
    }

    private void updateLikes(Film film) {
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ?", film.getId());
        if (film.getLikes() != null && !film.getLikes().isEmpty()) {
            String likeSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            Set<Long> validUserIds = film.getLikes().stream()
                    .filter(userId -> {
                        Integer count = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId);
                        return count != null && count > 0;
                    })
                    .collect(Collectors.toSet());
            for (Long userId : validUserIds) {
                try {
                    jdbcTemplate.update(likeSql, film.getId(), userId);
                } catch (Exception e) {
                    log.warn("Failed to add like from user {} to film {}: {}", userId, film.getId(), e.getMessage());
                }
            }
            film.setLikes(new HashSet<>(getLikesByFilmId(film.getId())));
        } else {
            film.setLikes(new HashSet<>());
        }
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(new Mpa(rs.getLong("mpa_id"), rs.getString("mpa_name")));
        return film;
    }

    private List<Genre> getGenresByFilmId(Long filmId) {
        String sql = "SELECT g.id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")), filmId);
        } catch (Exception e) {
            log.warn("Failed to retrieve genres for film {}: {}", filmId, e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Long> getLikesByFilmId(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        try {
            return jdbcTemplate.queryForList(sql, Long.class, filmId);
        } catch (Exception e) {
            log.warn("Failed to retrieve likes for film {}: {}", filmId, e.getMessage());
            return new ArrayList<>();
        }
    }
}