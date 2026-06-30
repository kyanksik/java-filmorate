package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreStorage genreStorage;

    @Override
    public Collection<GenreDto> findAll() {
        return genreStorage.findAll().stream()
                .map(GenreMapper::toDto)
                .toList();
    }

    @Override
    public GenreDto findById(int id) {
        return genreStorage.findById(id)
                .map(GenreMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
    }
}
