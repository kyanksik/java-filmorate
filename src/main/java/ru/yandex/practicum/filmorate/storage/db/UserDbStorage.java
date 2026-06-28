package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<User> userRowMapper;

    @Override
    public Collection<User> findAll() {
        return jdbc.query("SELECT user_id, email, login, name, birthday FROM users", userRowMapper);
    }

    @Override
    public User create(User user) {
        if (StringUtils.isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        Number id = insert.executeAndReturnKey(Map.of(
                "email", user.getEmail(),
                "login", user.getLogin(),
                "name", user.getName(),
                "birthday", user.getBirthday()
        ));
        user.setId(id.longValue());
        return user;
    }

    @Override
    public User update(User newUser) {
        if (!existById(newUser.getId())) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }
        if (StringUtils.isBlank(newUser.getName())) {
            newUser.setName(newUser.getLogin());
        }
        jdbc.update("UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?",
                newUser.getEmail(), newUser.getLogin(), newUser.getName(), newUser.getBirthday(), newUser.getId());
        return newUser;
    }

    @Override
    public User findById(Long id) {
        return jdbc.query("SELECT user_id, email, login, name, birthday FROM users WHERE user_id = ?",
                        userRowMapper, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    @Override
    public boolean existById(Long id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE user_id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        findById(userId);
        return jdbc.query("""
                SELECT u.user_id, u.email, u.login, u.name, u.birthday
                FROM users u
                JOIN friendships f ON u.user_id = f.friend_id
                WHERE f.user_id = ?
                """, userRowMapper, userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        findById(userId);
        findById(otherId);
        return jdbc.query("""
                SELECT u.user_id, u.email, u.login, u.name, u.birthday
                FROM users u
                JOIN friendships f1 ON u.user_id = f1.friend_id AND f1.user_id = ?
                JOIN friendships f2 ON u.user_id = f2.friend_id AND f2.user_id = ?
                """, userRowMapper, userId, otherId);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        // дружба односторонняя: в список добавляется только userId -> friendId.
        // если ответная заявка уже есть, дружба становится подтверждённой с обеих сторон.
        boolean reciprocal = friendshipExists(friendId, userId);
        jdbc.update("MERGE INTO friendships (user_id, friend_id, confirmed) KEY (user_id, friend_id) VALUES (?, ?, ?)",
                userId, friendId, reciprocal);
        if (reciprocal) {
            jdbc.update("UPDATE friendships SET confirmed = TRUE WHERE user_id = ? AND friend_id = ?",
                    friendId, userId);
        }
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        jdbc.update("DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", userId, friendId);
        // ответная заявка (если была) снова становится неподтверждённой
        jdbc.update("UPDATE friendships SET confirmed = FALSE WHERE user_id = ? AND friend_id = ?",
                friendId, userId);
    }

    @Override
    public void delete(long id) {
        jdbc.update("DELETE FROM users WHERE user_id = ?", id);
    }

    private boolean friendshipExists(Long userId, Long friendId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class, userId, friendId);
        return count != null && count > 0;
    }
}
