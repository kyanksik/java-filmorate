package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorServiceImpl implements DirectorService {

    private final DirectorStorage directorStorage;

    @Override
    public Collection<DirectorDto> findAll() {
        return directorStorage.findAll().stream()
                .map(DirectorMapper::toDto)
                .toList();
    }

    @Override
    public DirectorDto findById(long id) {
        return DirectorMapper.toDto(getOrThrow(id));
    }

    @Override
    public DirectorDto create(DirectorDto director) {
        Director created = directorStorage.create(DirectorMapper.toModel(director));
        return DirectorMapper.toDto(created);
    }

    @Override
    public DirectorDto update(DirectorDto director) {
        if (director.getId() == null || !directorStorage.existsById(director.getId())) {
            throw new NotFoundException("Режиссёр с id " + (director.getId()) + " не найден");
        }
        Director updated = directorStorage.update(DirectorMapper.toModel(director));
        return DirectorMapper.toDto(updated);
    }

    @Override
    public void delete(long id) {
        getOrThrow(id);
        directorStorage.delete(id);
    }

    private Director getOrThrow(long id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id " + id + " не найден"));
    }
}
