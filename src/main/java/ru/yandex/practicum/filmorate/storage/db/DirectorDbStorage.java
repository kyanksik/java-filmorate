package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<Director> directorRowMapper;

    @Override
    public Collection<Director> findAll() {
        return jdbc.query("SELECT director_id, name FROM directors ORDER BY director_id", directorRowMapper);
    }

    @Override
    public Optional<Director> findById(long id) {
        return jdbc.query("SELECT director_id, name FROM directors WHERE director_id = ?", directorRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsById(long id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM directors WHERE director_id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Director create(Director director) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc)
                .withTableName("directors")
                .usingGeneratedKeyColumns("director_id");
        Number id = insert.executeAndReturnKey(Map.of("name", director.getName()));
        director.setId(id.longValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        jdbc.update("UPDATE directors SET name = ? WHERE director_id = ?",
                director.getName(), director.getId());
        return director;
    }

    @Override
    public void delete(long id) {
        jdbc.update("DELETE FROM directors WHERE director_id = ?", id);
    }
}
