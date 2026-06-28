package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

public final class EventMapper {

    private EventMapper() {
    }

    public static EventDto toDto(Event event) {
        return new EventDto(
                event.getEventId(),
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType(),
                event.getOperation(),
                event.getEntityId());
    }
}
