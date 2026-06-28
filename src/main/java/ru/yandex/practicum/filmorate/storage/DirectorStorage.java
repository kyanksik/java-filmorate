package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {

    Collection<Director> findAll();

    Optional<Director> findById(long id);

    boolean existsById(long id);

    Director create(Director director);

    Director update(Director director);

    void delete(long id);
}
