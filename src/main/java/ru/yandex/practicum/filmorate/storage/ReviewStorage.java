package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    void delete(long id);

    Optional<Review> findById(long id);

    boolean existsById(long id);

    Collection<Review> getByFilm(Long filmId, int count);

    void addReaction(long reviewId, long userId, boolean useful);

    void removeReaction(long reviewId, long userId);
}
