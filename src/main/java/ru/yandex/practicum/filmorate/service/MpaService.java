package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.dto.MpaDto;

import java.util.Collection;

public interface MpaService {

    Collection<MpaDto> findAll();

    MpaDto findById(int id);
}
