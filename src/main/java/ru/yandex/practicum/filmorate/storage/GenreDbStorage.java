package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getLong("id"), rs.getString("name"));
    }
}