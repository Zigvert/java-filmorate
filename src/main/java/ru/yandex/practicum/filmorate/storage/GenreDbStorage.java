package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {
    private static final Logger log = LoggerFactory.getLogger(GenreDbStorage.class);
    private final JdbcTemplate jdbcTemplate;

    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY id";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre);
        log.info("Retrieved all genres: {}", genres);
        return genres;
    }

    public Optional<Genre> getGenreById(Long id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        try {
            List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre, id);
            if (genres.isEmpty()) {
                log.info("Genre with id {} not found", id);
                return Optional.empty();
            }
            log.info("Retrieved genre: {}", genres.get(0));
            return Optional.of(genres.get(0));
        } catch (Exception e) {
            log.error("Error retrieving genre with id {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public List<Genre> getGenresByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = String.format("SELECT * FROM genres WHERE id IN (%s)", inSql);
        try {
            List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre, ids.toArray());
            log.info("Retrieved genres for ids {}: {}", ids, genres);
            return genres;
        } catch (Exception e) {
            log.error("Error retrieving genres for ids {}: {}", ids, e.getMessage());
            throw new RuntimeException("Failed to retrieve genres", e);
        }
    }

    public Genre createGenre(Genre genre) {
        String sql = "INSERT INTO genres (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
                stmt.setString(1, genre.getName());
                return stmt;
            }, keyHolder);
            Long generatedId = keyHolder.getKey().longValue();
            genre.setId(generatedId);
            log.info("Created genre: {}", genre);
            return genre;
        } catch (Exception e) {
            log.error("Error creating genre: {}", e.getMessage());
            throw new RuntimeException("Failed to create genre", e);
        }
    }

    public Genre updateGenre(Genre genre) {
        String sql = "UPDATE genres SET name = ? WHERE id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, genre.getName(), genre.getId());
            if (rowsAffected == 0) {
                log.info("Genre with id {} not found for update", genre.getId());
                throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
            }
            log.info("Updated genre: {}", genre);
            return genre;
        } catch (Exception e) {
            log.error("Error updating genre with id {}: {}", genre.getId(), e.getMessage());
            throw new RuntimeException("Failed to update genre", e);
        }
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getLong("id"), rs.getString("name"));
    }
}