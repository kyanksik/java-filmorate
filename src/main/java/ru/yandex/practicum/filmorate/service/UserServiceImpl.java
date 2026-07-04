package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final EventService eventService;

    @Override
    public Collection<UserDto> findAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public UserDto create(UserDto user) {
        return UserMapper.toDto(userStorage.create(UserMapper.toModel(user)));
    }

    @Override
    public UserDto update(UserDto newUser) {
        return UserMapper.toDto(userStorage.update(UserMapper.toModel(newUser)));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        userStorage.addFriend(userId, friendId);
        eventService.addEvent(userId, EventType.FRIEND, Operation.ADD, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        userStorage.deleteFriend(userId, friendId);
        eventService.addEvent(userId, EventType.FRIEND, Operation.REMOVE, friendId);
    }

    @Override
    public Collection<UserDto> getFriends(Long userId) {
        return userStorage.getFriends(userId).stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public Collection<UserDto> getCommonFriends(Long userId, Long otherId) {
        return userStorage.getCommonFriends(userId, otherId).stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public UserDto findById(Long id) {
        return UserMapper.toDto(userStorage.findById(id));
    }

    @Override
    public void delete(long id) {
        userStorage.findById(id);
        userStorage.delete(id);
    }

    @Override
    public Collection<EventDto> getFeed(long userId) {
        userStorage.findById(userId);
        return eventService.getFeed(userId).stream()
                .map(EventMapper::toDto)
                .toList();
    }
}
