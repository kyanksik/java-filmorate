package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.dto.FilmDto;

import java.util.Collection;

public interface FilmService {

    Collection<FilmDto> findAll();

    FilmDto create(FilmDto film);

    FilmDto update(FilmDto newFilm);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<FilmDto> getPopular(int count, Integer genreId, Integer year);

    Collection<FilmDto> getFilmsByDirector(long directorId, String sortBy);

    Collection<FilmDto> search(String query, String by);

    Collection<FilmDto> getCommon(long userId, long friendId);

    Collection<FilmDto> getRecommendations(long userId);

    FilmDto findById(Long id);

    void delete(long id);
}
