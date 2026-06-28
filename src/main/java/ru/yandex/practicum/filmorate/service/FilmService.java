package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmService {

    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film newFilm);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<Film> getPopular(int count, Integer genreId, Integer year);

    Collection<Film> getFilmsByDirector(long directorId, String sortBy);

    Collection<Film> search(String query, String by);

    Collection<Film> getCommon(long userId, long friendId);

    Collection<Film> getRecommendations(long userId);

    Film findById(Long id);
}
