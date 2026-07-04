package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

public final class DirectorMapper {

    private DirectorMapper() {
    }

    public static DirectorDto toDto(Director director) {
        if (director == null) {
            return null;
        }
        return new DirectorDto(director.getId(), director.getName());
    }

    public static Director toModel(DirectorDto dto) {
        if (dto == null) {
            return null;
        }
        return new Director(dto.getId(), dto.getName());
    }
}
