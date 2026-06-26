package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final String BASE_SELECT = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_id, m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
            """;

    private final JdbcTemplate jdbc;
    private final RowMapper<Film> filmRowMapper;
    private final GenreRowMapper genreRowMapper;

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(BASE_SELECT, filmRowMapper);
        loadGenres(films);
        return films;
    }

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        Map<String, Object> params = new HashMap<>();
        params.put("name", film.getName());
        params.put("description", film.getDescription());
        params.put("release_date", film.getReleaseDate());
        params.put("duration", film.getDuration());
        params.put("mpa_id", film.getMpa() == null ? null : film.getMpa().getId());
        Number id = insert.executeAndReturnKey(params);
        film.setId(id.longValue());

        saveGenres(film);
        return findById(film.getId());
    }

    @Override
    public Film update(Film newFilm) {
        if (!existsById(newFilm.getId())) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
        jdbc.update("""
                        UPDATE films
                        SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                        WHERE film_id = ?
                        """,
                newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(), newFilm.getDuration(),
                newFilm.getMpa() == null ? null : newFilm.getMpa().getId(), newFilm.getId());

        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", newFilm.getId());
        saveGenres(newFilm);
        return findById(newFilm.getId());
    }

    @Override
    public Film findById(Long id) {
        Film film = jdbc.query(BASE_SELECT + " WHERE f.film_id = ?", filmRowMapper, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
        loadGenres(List.of(film));
        return film;
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM films WHERE film_id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Collection<Film> getPopular(int count) {
        List<Film> films = jdbc.query(BASE_SELECT + """
                LEFT JOIN film_likes fl ON f.film_id = fl.film_id
                GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
                ORDER BY COUNT(fl.user_id) DESC
                LIMIT ?
                """, filmRowMapper, count);
        loadGenres(films);
        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update("MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)",
                filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbc.update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        List<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .distinct()
                .toList();
        jdbc.batchUpdate(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                genreIds,
                genreIds.size(),
                (ps, genreId) -> {
                    ps.setLong(1, film.getId());
                    ps.setInt(2, genreId);
                });
    }

    private void loadGenres(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        Map<Long, Film> filmsById = new HashMap<>();
        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>());
            filmsById.put(film.getId(), film);
        }
        String inSql = String.join(",", filmsById.keySet().stream().map(id -> "?").toList());
        jdbc.query("""
                        SELECT fg.film_id, g.genre_id, g.name
                        FROM film_genres fg
                        JOIN genres g ON fg.genre_id = g.genre_id
                        WHERE fg.film_id IN (%s)
                        ORDER BY g.genre_id
                        """.formatted(inSql),
                rs -> {
                    long filmId = rs.getLong("film_id");
                    Genre genre = genreRowMapper.mapRow(rs, 0);
                    filmsById.get(filmId).getGenres().add(genre);
                },
                filmsById.keySet().toArray());
    }
}
