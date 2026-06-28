package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;


import java.util.Collection;

public interface FilmStorage {

    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film newFilm);

    Film findById(Long id);

    boolean existsById(Long id);

    Collection<Film> getPopular(int count, Integer genreId, Integer year);

    Collection<Film> getByDirector(long directorId, String sortBy);

    Collection<Film> search(String query, String by);

    Collection<Film> getCommon(long userId, long friendId);

    Collection<Film> getRecommendations(long userId);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    void delete(long id);

}
