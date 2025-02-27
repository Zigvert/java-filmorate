package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private final List<User> users = new ArrayList<>();

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Создание пользователя: {}", user);
        user.setId(users.size() + 1);
        users.add(user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Обновление пользователя: {}", user);
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == user.getId()) {
                users.set(i, user);
                return user;
            }
        }
        log.warn("Пользователь с id={} не найден", user.getId());
        throw new IllegalArgumentException("Пользователь с id=" + user.getId() + " не найден");
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получение всех пользователей");
        return users;
    }
}