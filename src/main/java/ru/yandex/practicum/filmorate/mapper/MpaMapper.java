package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

public final class MpaMapper {

    private MpaMapper() {
    }

    public static MpaDto toDto(Mpa mpa) {
        if (mpa == null) {
            return null;
        }
        return new MpaDto(mpa.getId(), mpa.getName());
    }

    public static Mpa toModel(MpaDto dto) {
        if (dto == null) {
            return null;
        }
        return new Mpa(dto.getId(), dto.getName());
    }
}
