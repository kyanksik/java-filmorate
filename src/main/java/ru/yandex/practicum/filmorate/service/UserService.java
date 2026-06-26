package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public void addFriend(Long userId, Long friendId) {
        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        userStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        return userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        return userStorage.getCommonFriends(userId, otherId);
    }

    public User findById(Long id) {
        return userStorage.findById(id);
    }

}
