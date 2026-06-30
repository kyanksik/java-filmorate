package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.dto.DirectorDto;

import java.util.Collection;

public interface DirectorService {

    Collection<DirectorDto> findAll();

    DirectorDto findById(long id);

    DirectorDto create(DirectorDto director);

    DirectorDto update(DirectorDto director);

    void delete(long id);
}
