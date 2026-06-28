package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class FilmMapper {

    private FilmMapper() {
    }

    public static FilmDto toDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setMpa(MpaMapper.toDto(film.getMpa()));
        Set<GenreDto> genres = film.getGenres() == null ? new LinkedHashSet<>()
                : film.getGenres().stream()
                .map(GenreMapper::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        dto.setGenres(genres);
        return dto;
    }

    public static Film toModel(FilmDto dto) {
        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());
        film.setMpa(MpaMapper.toModel(dto.getMpa()));
        Set<Genre> genres = dto.getGenres() == null ? new LinkedHashSet<>()
                : dto.getGenres().stream()
                .map(GenreMapper::toModel)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(genres);
        return film;
    }
}
