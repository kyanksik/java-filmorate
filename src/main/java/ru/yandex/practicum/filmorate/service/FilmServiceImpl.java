package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;


@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Qualifier("filmDbStorage")
    private final FilmStorage filmsStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private final EventService eventService;

    @Override
    public Collection<FilmDto> findAll() {
        return toDtoList(filmsStorage.findAll());
    }

    @Override
    public FilmDto create(FilmDto dto) {
        Film film = FilmMapper.toModel(dto);
        validate(film);
        return FilmMapper.toDto(filmsStorage.create(film));
    }

    @Override
    public FilmDto update(FilmDto dto) {
        Film film = FilmMapper.toModel(dto);
        validate(film);
        return FilmMapper.toDto(filmsStorage.update(film));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        filmsStorage.findById(filmId);
        if (!userStorage.existById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        filmsStorage.addLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        filmsStorage.findById(filmId);
        if (!userStorage.existById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        filmsStorage.deleteLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }

    @Override
    public Collection<FilmDto> getPopular(int count, Integer genreId, Integer year) {
        return toDtoList(filmsStorage.getPopular(count, genreId, year));
    }

    @Override
    public Collection<FilmDto> getFilmsByDirector(long directorId, String sortBy) {
        if (!directorStorage.existsById(directorId)) {
            throw new NotFoundException("Режиссёр с id " + directorId + " не найден");
        }
        return toDtoList(filmsStorage.getByDirector(directorId, sortBy));
    }

    @Override
    public Collection<FilmDto> search(String query, String by) {
        return toDtoList(filmsStorage.search(query, by));
    }

    @Override
    public Collection<FilmDto> getCommon(long userId, long friendId) {
        if (!userStorage.existById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!userStorage.existById(friendId)) {
            throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        }
        return toDtoList(filmsStorage.getCommon(userId, friendId));
    }

    @Override
    public Collection<FilmDto> getRecommendations(long userId) {
        if (!userStorage.existById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        return toDtoList(filmsStorage.getRecommendations(userId));
    }

    @Override
    public FilmDto findById(Long id) {
        return FilmMapper.toDto(filmsStorage.findById(id));
    }

    @Override
    public void delete(long id) {
        filmsStorage.findById(id);
        filmsStorage.delete(id);
    }

    private Collection<FilmDto> toDtoList(Collection<Film> films) {
        return films.stream().map(FilmMapper::toDto).toList();
    }

    private void validate(Film film) {
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getMpa() != null && !mpaStorage.existsById(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг MPA с id " + film.getMpa().getId() + " не найден");
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (!genreStorage.existsById(genre.getId())) {
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                if (!directorStorage.existsById(director.getId())) {
                    throw new NotFoundException("Режиссёр с id " + director.getId() + " не найден");
                }
            }
        }
    }
}
