package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Создание пользователя: {}", user);
        user.setId(users.size() + 1);
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Обновление пользователя: {}", user);

        if (user.getId() <= 0 || !users.containsKey(user.getId())) {
            log.warn("Пользователь с id={} не найден или id некорректен", user.getId());
            throw new IllegalArgumentException("Пользователь с id=" + user.getId() + " не найден или id некорректен");
        }

        User existingUser = users.get(user.getId());

        if (user.getEmail() != null) {
            if (user.getEmail().isBlank()) {
                throw new ValidationException("Электронная почта не может быть пустой");
            }
            if (!user.getEmail().contains("@")) {
                throw new ValidationException("Электронная почта должна содержать символ @");
            }
            existingUser.setEmail(user.getEmail());
        }

        if (user.getLogin() != null) {
            if (user.getLogin().isBlank()) {
                throw new ValidationException("Логин не может быть пустым");
            }
            if (user.getLogin().contains(" ")) {
                throw new ValidationException("Логин не может содержать пробелы");
            }
            existingUser.setLogin(user.getLogin());
        }

        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }

        if (user.getBirthday() != null) {
            if (user.getBirthday().isAfter(LocalDate.now())) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
            existingUser.setBirthday(user.getBirthday());
        }

        return existingUser;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получение всех пользователей");
        return new ArrayList<>(users.values());
    }
}