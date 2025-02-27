package ru.yandex.practicum.filmorate.modelTest;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;

public class FilmValidationTest {

    private final Validator validator;

    public FilmValidationTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        assertTrue(validator.validate(film).isEmpty(), "Фильм с валидными данными не прошёл валидацию");
    }

    @Test
    public void testEmptyName() {
        Film film = new Film();
        film.setName(""); // Пустое имя
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertFalse(validator.validate(film).isEmpty(), "Фильм с пустым именем прошёл валидацию");
    }

    @Test
    public void testDescriptionTooLong() {
        Film film = new Film();
        film.setName("Movie");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertFalse(validator.validate(film).isEmpty(), "Фильм с описанием >200 символов прошёл валидацию");
    }

    @Test
    public void testNegativeDuration() {
        Film film = new Film();
        film.setName("Movie");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(-10);

        assertFalse(validator.validate(film).isEmpty(), "Фильм с отрицательной продолжительностью прошёл валидацию");
    }

    @Test
    public void testReleaseDateTooEarly() {
        Film film = new Film();
        film.setName("Old Movie");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> film.setReleaseDate(LocalDate.of(1895, 12, 27)),
                "Не выброшено исключение для даты релиза раньше 28.12.1895");
    }
}