package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.Collection;

public interface EventService {

    void addEvent(long userId, EventType eventType, Operation operation, long entityId);

    Collection<Event> getFeed(long userId);
}
