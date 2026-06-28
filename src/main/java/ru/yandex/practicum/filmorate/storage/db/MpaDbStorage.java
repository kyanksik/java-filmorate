package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<Mpa> mpaRowMapper;

    @Override
    public Collection<Mpa> findAll() {
        return jdbc.query("SELECT mpa_id, name FROM mpa_ratings ORDER BY mpa_id", mpaRowMapper);
    }

    @Override
    public Optional<Mpa> findById(int id) {
        return jdbc.query("SELECT mpa_id, name FROM mpa_ratings WHERE mpa_id = ?", mpaRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsById(int id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM mpa_ratings WHERE mpa_id = ?", Integer.class, id);
        return count != null && count > 0;
    }
}
