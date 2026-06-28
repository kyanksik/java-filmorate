package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.util.Collection;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<Event> eventRowMapper;

    @Override
    public Event add(Event event) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc)
                .withTableName("events")
                .usingGeneratedKeyColumns("event_id");
        Number id = insert.executeAndReturnKey(Map.of(
                "user_id", event.getUserId(),
                "entity_id", event.getEntityId(),
                "event_type", event.getEventType().name(),
                "operation", event.getOperation().name(),
                "created_at", event.getTimestamp()
        ));
        event.setEventId(id.longValue());
        return event;
    }

    @Override
    public Collection<Event> getByUser(long userId) {
        return jdbc.query("""
                SELECT event_id, user_id, entity_id, event_type, operation, created_at
                FROM events
                WHERE user_id = ?
                ORDER BY event_id
                """, eventRowMapper, userId);
    }
}
