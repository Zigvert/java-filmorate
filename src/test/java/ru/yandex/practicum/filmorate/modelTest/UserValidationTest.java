package ru.yandex.practicum.filmorate.modelTest;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import ru.yandex.practicum.filmorate.model.User;
import static org.junit.jupiter.api.Assertions.*;


public class UserValidationTest {

    private final Validator validator;

    public UserValidationTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertTrue(validator.validate(user).isEmpty(), "Пользователь с валидными данными не прошёл валидацию");
    }

    @Test
    public void testInvalidEmail() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertFalse(validator.validate(user).isEmpty(), "Пользователь с неверным email прошёл валидацию");
    }

    @Test
    public void testEmptyLogin() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertFalse(validator.validate(user).isEmpty(), "Пользователь с пустым логином прошёл валидацию");
    }

    @Test
    public void testFutureBirthday() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertFalse(validator.validate(user).isEmpty(), "Пользователь с датой рождения в будущем прошёл валидацию");
    }

    @Test
    public void testNameSubstitution() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertEquals("userlogin", user.getName(), "Имя не было подставлено из логина");
    }
}