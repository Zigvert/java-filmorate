package ru.yandex.practicum.filmorate.storageTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(GenreDbStorage.class)
class GenreDbStorageTests {
    private final GenreDbStorage genreStorage;

    @Test
    void testGetAllGenres() {
        List<Genre> genres = genreStorage.getAllGenres();

        assertThat(genres).isNotEmpty();
    }

    @Test
    void testGetGenreById() {
        Optional<Genre> genreOptional = genreStorage.getGenreById(1L);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(g -> assertThat(g.getId()).isEqualTo(1L));
    }

    @Test
    void testGetGenresByIds() {
        List<Long> genreIds = Arrays.asList(1L, 2L);
        List<Genre> genres = genreStorage.getGenresByIds(genreIds);

        assertThat(genres)
                .isNotNull()
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1L, 2L);

        genreIds = Arrays.asList(1L, 999L);
        genres = genreStorage.getGenresByIds(genreIds);

        assertThat(genres)
                .isNotNull()
                .hasSize(1)
                .extracting(Genre::getId)
                .containsExactly(1L);

        genres = genreStorage.getGenresByIds(Collections.emptyList());

        assertThat(genres)
                .isNotNull()
                .isEmpty();
    }
}