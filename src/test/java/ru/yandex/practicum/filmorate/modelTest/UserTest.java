package ru.yandex.practicum.filmorate.modelTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserTest {

    private Validator validator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
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

        assertTrue(validator.validate(user).isEmpty(), "Валидный пользователь не прошёл валидацию");
    }

    @Test
    public void testNullEmail() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertFalse(validator.validate(user).isEmpty(), "Пользователь с null email прошёл валидацию");
    }

    @Test
    public void testEmptyEmail() {
        User user = new User();
        user.setEmail("");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertFalse(validator.validate(user).isEmpty(), "Пользователь с пустым email прошёл валидацию");
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
    public void testNullLogin() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin(null); // Null логин
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertFalse(validator.validate(user).isEmpty(), "Пользователь с null логином прошёл валидацию");
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
    public void testLoginWithSpaces() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertFalse(validator.validate(user).isEmpty(), "Пользователь с пробелами в логине прошёл валидацию");
    }

    @Test
    public void testNullBirthday() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(null);

        assertFalse(validator.validate(user).isEmpty(), "Пользователь с null датой рождения прошёл валидацию");
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
    public void testNullNameSubstitution() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertEquals("userlogin", user.getName(), "Имя не было подставлено из логина при null");
    }

    @Test
    public void testEmptyNameSubstitution() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertEquals("userlogin", user.getName(), "Имя не было подставлено из логина при пустом значении");
    }

    @Test
    public void testAddValidUser() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    @Test
    public void testAddUserWithNullEmail() throws Exception {
        User user = new User();
        user.setEmail(null);
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddUserWithInvalidEmail() throws Exception {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddUserWithEmptyLogin() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddUserWithLoginWithSpaces() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddUserWithNullBirthday() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddUserWithFutureBirthday() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }
}