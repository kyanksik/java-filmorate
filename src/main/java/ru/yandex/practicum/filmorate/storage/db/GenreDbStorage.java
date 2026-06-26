package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<Genre> genreRowMapper;

    @Override
    public Collection<Genre> findAll() {
        return jdbc.query("SELECT genre_id, name FROM genres ORDER BY genre_id", genreRowMapper);
    }

    @Override
    public Optional<Genre> findById(int id) {
        return jdbc.query("SELECT genre_id, name FROM genres WHERE genre_id = ?", genreRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsById(int id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM genres WHERE genre_id = ?", Integer.class, id);
        return count != null && count > 0;
    }
}
