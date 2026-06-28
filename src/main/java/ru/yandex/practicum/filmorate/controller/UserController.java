package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> findAll() {
        return userService.findAll().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto user) {
        return UserMapper.toDto(userService.create(UserMapper.toModel(user)));
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody UserDto newUser) {
        return UserMapper.toDto(userService.update(UserMapper.toModel(newUser)));
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) {
        return UserMapper.toDto(userService.findById(id));
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Удаление из друзей: {} и {}", id, friendId);
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<UserDto> getFriends(@PathVariable Long id) {
        log.info("Запрос списка друзей пользователя id={}", id);
        return userService.getFriends(id).stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<UserDto> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Запрос общих друзей пользователей id={} и id={}", id, otherId);
        return userService.getCommonFriends(id, otherId).stream()
                .map(UserMapper::toDto)
                .toList();
    }

}
