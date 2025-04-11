package ru.yandex.practicum.filmorate.storageTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(MpaDbStorage.class)
class MpaDbStorageTests {
    private final MpaDbStorage mpaStorage;

    @Test
    void testGetAllMpa() {
        List<Mpa> mpaList = mpaStorage.getAllMpa();

        assertThat(mpaList).isNotEmpty();
    }

    @Test
    void testGetMpaById() {
        Optional<Mpa> mpaOptional = mpaStorage.getMpaById(1L);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(m -> assertThat(m.getId()).isEqualTo(1L));
    }
}