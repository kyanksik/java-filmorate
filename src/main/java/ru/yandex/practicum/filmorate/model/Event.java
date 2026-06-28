package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Event {
    private Long eventId;
    private long timestamp;
    private long userId;
    private EventType eventType;
    private Operation operation;
    private long entityId;
}
