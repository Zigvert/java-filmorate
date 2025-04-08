package ru.yandex.practicum.filmorate.storageTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(UserDbStorage.class)
@ComponentScan("ru.yandex.practicum.filmorate.storage")
class UserDbStorageTests {
    private final UserDbStorage userStorage;

    @Test
    void testAddUser() {
        User user = new User(null, null, "test@example.com", "testuser", "Test User", LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.addUser(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isGreaterThan(0);
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testUpdateUser() {
        User user = new User(null, null, "test@example.com", "testuser", "Test User", LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.addUser(user);

        savedUser.setName("Updated User");
        User updatedUser = userStorage.updateUser(savedUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated User");
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void testGetUserById() {
        User user = new User(null, null, "test@example.com", "testuser", "Test User", LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.addUser(user);

        Optional<User> userOptional = userStorage.getUserById(savedUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> assertThat(u.getId()).isEqualTo(savedUser.getId()));
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User(null, null, "user1@example.com", "user1", "User1", LocalDate.of(1990, 1, 1));
        User user2 = new User(null, null, "user2@example.com", "user2", "User2", LocalDate.of(1990, 2, 2));
        userStorage.addUser(user1);
        userStorage.addUser(user2);

        List<User> users = userStorage.getAllUsers();

        assertThat(users).hasSize(2);
    }

    @Test
    void testAddFriendAndGetFriend() {
        User user1 = new User(null, null, "user1@example.com", "user1", "User1", LocalDate.of(1990, 1, 1));
        User user2 = new User(null, null, "user2@example.com", "user2", "User2", LocalDate.of(1990, 2, 2));
        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId());
        List<User> friends = userStorage.getFriends(savedUser1.getId());

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(savedUser2.getId());
    }

    @Test
    void testRemoveFriend() {
        User user1 = new User(null, null, "user1@example.com", "user1", "User1", LocalDate.of(1990, 1, 1));
        User user2 = new User(null, null, "user2@example.com", "user2", "User2", LocalDate.of(1990, 2, 2));
        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId());
        userStorage.removeFriend(savedUser1.getId(), savedUser2.getId());
        List<User> friends = userStorage.getFriends(savedUser1.getId());

        assertThat(friends).isEmpty();
    }

    @Test
    void testDeleteUser() {
        User user = new User(null, null, "test@example.com", "testuser", "Test User", LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.addUser(user);

        userStorage.deleteUser(savedUser.getId());
        Optional<User> userOptional = userStorage.getUserById(savedUser.getId());

        assertThat(userOptional).isEmpty();
    }
}