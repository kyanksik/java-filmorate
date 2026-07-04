package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.dto.UserDto;

import java.util.Collection;

public interface UserService {

    Collection<UserDto> findAll();

    UserDto create(UserDto user);

    UserDto update(UserDto newUser);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    Collection<UserDto> getFriends(Long userId);

    Collection<UserDto> getCommonFriends(Long userId, Long otherId);

    UserDto findById(Long id);

    void delete(long id);

    Collection<EventDto> getFeed(long userId);
}
