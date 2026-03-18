package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;


@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmsStorage;
    private final UserStorage userStorage;

    public Collection<Film> findAll() {
        return filmsStorage.findAll();
    }

    public Film create(Film film) {
        return filmsStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmsStorage.update(newFilm);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmsStorage.findById(filmId);
        if (!userStorage.existById(userId)) throw new NotFoundException("Пользователь с id " + userId + " не найден");
        film.getLikes().add(userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = filmsStorage.findById(filmId);
        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Лайк от пользователя " + userId + " не найден");
        }
        film.getLikes().remove(userId);
    }

    public Collection<Film> getPopular(int count) {
        return filmsStorage.getPopular(count);
    }

    public Film findById(Long id) {
        return filmsStorage.findById(id);
    }

}