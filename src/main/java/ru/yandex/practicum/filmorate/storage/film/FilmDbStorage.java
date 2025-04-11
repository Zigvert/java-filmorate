package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

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
    private final GenreDbStorage genreDbStorage;

    @Override
    public Film addFilm(Film film) {
        log.info("Adding new film: {}", film.getName());
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
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
            updateGenres(filmId, film.getGenres());
            log.info("Film added with id {}: {}", filmId, film);
            return getFilmById(filmId).orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден после добавления"));
        } catch (DataAccessException e) {
            log.error("Error adding film: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Updating film with id {}: {}", film.getId(), film.getName());
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

        try {
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

            updateGenres(film.getId(), film.getGenres());
            updateLikes(film.getId(), film.getLikes());
            return getFilmById(film.getId())
                    .orElseThrow(() -> new NotFoundException("Фильм с id=" + film.getId() + " не найден после обновления"));
        } catch (DataAccessException e) {
            log.error("Error updating film with id {}: {}", film.getId(), e.getMessage(), e);
            throw e; // Пробрасываем оригинальное исключение
        }
    }

    @Override
    public void deleteFilm(Long id) {
        log.info("Deleting film with id {}", id);
        String sql = "DELETE FROM films WHERE id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, id);
            if (rowsAffected == 0) {
                log.warn("Film with id {} not found", id);
                throw new NotFoundException("Фильм с id=" + id + " не найден");
            }
            log.info("Film with id {} deleted", id);
        } catch (DataAccessException e) {
            log.error("Error deleting film with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        log.info("Retrieving film with id {}", id);
        String sql = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "WHERE f.id = ?";

        try {
            List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);
            Film film = films.isEmpty() ? null : films.get(0);
            if (film != null) {
                film.setGenres(getGenresByFilmId(id));
                film.setLikes(new HashSet<>(getLikesByFilmId(id)));
                log.info("Found film with id {}: {}", id, film);
            } else {
                log.info("Film with id {} not found", id);
            }
            return Optional.ofNullable(film);
        } catch (DataAccessException e) {
            log.error("Error retrieving film with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<Film> getAllFilms() {
        log.info("Retrieving all films");
        String sql = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.id";
        try {
            List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
            if (!films.isEmpty()) {
                Map<Long, List<Genre>> genresByFilmId = getGenresForFilms(films);
                Map<Long, Set<Long>> likesByFilmId = getLikesForFilms(films);
                for (Film film : films) {
                    film.setGenres(genresByFilmId.getOrDefault(film.getId(), new ArrayList<>()));
                    film.setLikes(likesByFilmId.getOrDefault(film.getId(), new HashSet<>()));
                }
            }
            log.info("Retrieved {} films", films.size());
            return films;
        } catch (DataAccessException e) {
            log.error("Error retrieving all films: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.info("Adding like to film {} by user {}", filmId, userId);
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, filmId, userId);
            log.info("Like added to film {} by user {}", filmId, userId);
        } catch (DataAccessException e) {
            log.error("Error adding like to film {} by user {}: {}", filmId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        log.info("Removing like from film {} by user {}", filmId, userId);
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, filmId, userId);
            if (rowsAffected == 0) {
                log.warn("Like not found for film {} by user {}", filmId, userId);
            }
            log.info("Like removed from film {} by user {}", filmId, userId);
        } catch (DataAccessException e) {
            log.error("Error removing like from film {} by user {}: {}", filmId, userId, e.getMessage(), e);
            throw e;
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
        try {
            List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);
            if (!films.isEmpty()) {
                Map<Long, List<Genre>> genresByFilmId = getGenresForFilms(films);
                Map<Long, Set<Long>> likesByFilmId = getLikesForFilms(films);
                for (Film film : films) {
                    film.setGenres(genresByFilmId.getOrDefault(film.getId(), new ArrayList<>()));
                    film.setLikes(likesByFilmId.getOrDefault(film.getId(), new HashSet<>()));
                }
            }
            log.info("Retrieved {} popular films", films.size());
            return films;
        } catch (DataAccessException e) {
            log.error("Error retrieving popular films: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void updateGenres(Long filmId, List<Genre> genres) {
        try {
            jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
            if (genres != null && !genres.isEmpty()) {
                List<Long> genreIds = genres.stream()
                        .map(Genre::getId)
                        .distinct()
                        .collect(Collectors.toList());
                List<Genre> existingGenres = genreDbStorage.getGenresByIds(genreIds);
                if (existingGenres.size() != genreIds.size()) {
                    List<Long> existingIds = existingGenres.stream()
                            .map(Genre::getId)
                            .collect(Collectors.toList());
                    List<Long> missingIds = genreIds.stream()
                            .filter(id -> !existingIds.contains(id))
                            .collect(Collectors.toList());
                    throw new NotFoundException("Жанры с id=" + missingIds + " не найдены");
                }
                String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
                List<Object[]> batchArgs = genres.stream()
                        .distinct()
                        .map(genre -> new Object[]{filmId, genre.getId()})
                        .collect(Collectors.toList());
                jdbcTemplate.batchUpdate(genreSql, batchArgs);
            }
        } catch (DataAccessException e) {
            log.error("Error updating genres for film {}: {}", filmId, e.getMessage(), e);
            throw e;
        }
    }

    private void updateLikes(Long filmId, Set<Long> likes) {
        try {
            jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ?", filmId);
            if (likes != null && !likes.isEmpty()) {
                String likeSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
                List<Object[]> batchArgs = likes.stream()
                        .map(userId -> new Object[]{filmId, userId})
                        .collect(Collectors.toList());
                jdbcTemplate.batchUpdate(likeSql, batchArgs);
            }
        } catch (DataAccessException e) {
            log.error("Error updating likes for film {}: {}", filmId, e.getMessage(), e);
            throw e;
        }
    }

    private Map<Long, List<Genre>> getGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return new HashMap<>();
        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        String sql = "SELECT fg.film_id, g.id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + String.join(",", Collections.nCopies(filmIds.size(), "?")) + ")";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Long filmId = rs.getLong("film_id");
                Genre genre = new Genre(rs.getLong("id"), rs.getString("name"));
                return new AbstractMap.SimpleEntry<>(filmId, genre);
            }, filmIds.toArray()).stream().collect(Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toList())
            ));
        } catch (DataAccessException e) {
            log.error("Error retrieving genres for films: {}", e.getMessage(), e);
            throw e;
        }
    }

    private Map<Long, Set<Long>> getLikesForFilms(List<Film> films) {
        if (films.isEmpty()) return new HashMap<>();
        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        String sql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" +
                String.join(",", Collections.nCopies(filmIds.size(), "?")) + ")";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Long filmId = rs.getLong("film_id");
                Long userId = rs.getLong("user_id");
                return new AbstractMap.SimpleEntry<>(filmId, userId);
            }, filmIds.toArray()).stream().collect(Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
            ));
        } catch (DataAccessException e) {
            log.error("Error retrieving likes for films: {}", e.getMessage(), e);
            throw e;
        }
    }

    private List<Genre> getGenresByFilmId(Long filmId) {
        String sql = "SELECT g.id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")), filmId);
        } catch (DataAccessException e) {
            log.error("Error retrieving genres for film {}: {}", filmId, e.getMessage(), e);
            throw e;
        }
    }

    private List<Long> getLikesByFilmId(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        try {
            return jdbcTemplate.queryForList(sql, Long.class, filmId);
        } catch (DataAccessException e) {
            log.error("Error retrieving likes for film {}: {}", filmId, e.getMessage(), e);
            throw e;
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
}