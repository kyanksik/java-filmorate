package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

public final class GenreMapper {

    private GenreMapper() {
    }

    public static GenreDto toDto(Genre genre) {
        if (genre == null) {
            return null;
        }
        return new GenreDto(genre.getId(), genre.getName());
    }

    public static Genre toModel(GenreDto dto) {
        if (dto == null) {
            return null;
        }
        return new Genre(dto.getId(), dto.getName());
    }
}
