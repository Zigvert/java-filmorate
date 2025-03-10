package ru.yandex.practicum.filmorate.modelTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(UserController.class)
public class UserTest {

    private Validator validator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        when(userService.addUser(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getEmail() == null || !user.getEmail().contains("@")) {
                throw new ValidationException("Email должен содержать @");
            }
            if (user.getLogin() == null || user.getLogin().contains(" ")) {
                throw new ValidationException("Логин не должен содержать пробелы");
            }
            if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                user.setName(user.getLogin());
            }
            user.setId(1L);
            return user;
        });

        when(userService.getUserById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            if (id <= 0) {
                throw new NotFoundException("Пользователь с ID " + id + " не найден");
            }
            User user = new User();
            user.setId(id);
            user.setEmail("user" + id + "@example.com");
            user.setLogin("user" + id);
            user.setName("User " + id);
            user.setBirthday(LocalDate.of(1990, 1, 1));
            return user;
        });

        when(userService.getFriends(anyLong())).thenReturn(Collections.emptyList());
        when(userService.getCommonFriends(anyLong(), anyLong())).thenReturn(Collections.emptyList());
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
        user.setLogin(null);
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
                .andExpect(status().isOk()); // Исправлено с isCreated() на isOk()
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

    @Test
    public void testAddFriend() throws Exception {
        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test
    public void testAddFriendWithInvalidUserId() throws Exception {
        doThrow(new NotFoundException("Пользователь с ID -1 не найден"))
                .when(userService).addFriend(eq(-1L), anyLong());

        mockMvc.perform(put("/users/-1/friends/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRemoveFriend() throws Exception {
        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test
    public void testRemoveFriendWithInvalidUserId() throws Exception {
        doThrow(new NotFoundException("Пользователь с ID -1 не найден"))
                .when(userService).removeFriend(eq(-1L), anyLong());

        mockMvc.perform(delete("/users/-1/friends/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetFriends() throws Exception {
        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void testGetCommonFriends() throws Exception {
        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}