package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        User addedUser = userStorage.addUser(user);
        log.info("Added user: {}", addedUser);
        return addedUser;
    }

    public User updateUser(User user) {
        if (user.getId() == null || !userStorage.getUserById(user.getId()).isPresent()) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        User updatedUser = userStorage.updateUser(user);
        log.info("Updated user: {}", updatedUser);
        return updatedUser;
    }

    public List<User> getAllUsers() {
        List<User> users = userStorage.getAllUsers();
        log.info("Returning all users: {}", users);
        return users;
    }

    public User getUserById(Long id) {
        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
        log.info("Returning user: {}", user);
        return user;
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Пользователь не может добавить себя в друзья");
        }
        getUserById(userId);
        getUserById(friendId);
        userStorage.addFriend(userId, friendId);
        log.info("User {} added friend {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Пользователь не может удалить себя из друзей");
        }
        getUserById(userId);
        getUserById(friendId);
        userStorage.removeFriend(userId, friendId);
        log.info("User {} removed friend {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        List<User> friends = userStorage.getFriends(userId);
        log.info("Returning friends of user {}: {}", userId, friends);
        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        getUserById(userId);
        getUserById(otherId);
        List<User> commonFriends = userStorage.getCommonFriends(userId, otherId);
        log.info("Returning common friends between user {} and user {}: {}", userId, otherId, commonFriends);
        return commonFriends;
    }
}