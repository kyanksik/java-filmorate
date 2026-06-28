package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmService {

    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film newFilm);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<Film> getPopular(int count);

    Collection<Film> getFilmsByDirector(long directorId, String sortBy);

    Film findById(Long id);
}
