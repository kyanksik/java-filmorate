package ru.yandex.practicum.filmorate.controller;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j

public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Получаем список все пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User newUser) {
        validate(newUser);
        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        log.info("Новый пользователь создан");
        return newUser;
    }

    private void validate(User user) {
        if (StringUtils.isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Ищем пользователя по ID");
        if (exists(newUser)) {
            users.put(newUser.getId(), newUser);
            return newUser;
        }
        log.warn("Пользователь не найден");
        throw new NotFoundException("Пользователя с таким id = " + newUser.getId() + " нет");
    }

    private long getNextId() {
        return users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }

    private boolean exists(User user) {
        return users.containsKey(user.getId());
    }

}
