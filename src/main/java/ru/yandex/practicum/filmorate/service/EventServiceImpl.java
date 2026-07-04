package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventStorage eventStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    @Override
    public void addEvent(long userId, EventType eventType, Operation operation, long entityId) {
        Event event = new Event();
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setOperation(operation);
        event.setEntityId(entityId);
        event.setTimestamp(System.currentTimeMillis());
        eventStorage.add(event);
    }

    @Override
    public Collection<Event> getFeed(long userId) {
        if (!userStorage.existById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        return eventStorage.getByUser(userId);
    }
}
