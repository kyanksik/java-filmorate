package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorServiceImpl implements DirectorService {

    private final DirectorStorage directorStorage;

    @Override
    public Collection<Director> findAll() {
        return directorStorage.findAll();
    }

    @Override
    public Director findById(long id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id " + id + " не найден"));
    }

    @Override
    public Director create(Director director) {
        return directorStorage.create(director);
    }

    @Override
    public Director update(Director director) {
        if (director.getId() == null || !directorStorage.existsById(director.getId())) {
            throw new NotFoundException("Режиссёр с id " + director.getId() + " не найден");
        }
        return directorStorage.update(director);
    }

    @Override
    public void delete(long id) {
        findById(id);
        directorStorage.delete(id);
    }
}
