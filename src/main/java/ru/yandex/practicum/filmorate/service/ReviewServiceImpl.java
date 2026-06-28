package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewStorage reviewStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    @Override
    public Review create(Review review) {
        checkUser(review.getUserId());
        checkFilm(review.getFilmId());
        return reviewStorage.create(review);
    }

    @Override
    public Review update(Review review) {
        getOrThrow(review.getReviewId());
        return reviewStorage.update(review);
    }

    @Override
    public void delete(long id) {
        getOrThrow(id);
        reviewStorage.delete(id);
    }

    @Override
    public Review findById(long id) {
        return getOrThrow(id);
    }

    @Override
    public Collection<Review> getByFilm(Long filmId, int count) {
        if (filmId != null) {
            checkFilm(filmId);
        }
        return reviewStorage.getByFilm(filmId, count);
    }

    @Override
    public void addLike(long reviewId, long userId) {
        getOrThrow(reviewId);
        checkUser(userId);
        reviewStorage.addReaction(reviewId, userId, true);
    }

    @Override
    public void addDislike(long reviewId, long userId) {
        getOrThrow(reviewId);
        checkUser(userId);
        reviewStorage.addReaction(reviewId, userId, false);
    }

    @Override
    public void deleteLike(long reviewId, long userId) {
        getOrThrow(reviewId);
        reviewStorage.removeReaction(reviewId, userId);
    }

    @Override
    public void deleteDislike(long reviewId, long userId) {
        getOrThrow(reviewId);
        reviewStorage.removeReaction(reviewId, userId);
    }

    private Review getOrThrow(long id) {
        return reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден"));
    }

    private void checkUser(Long userId) {
        if (userId == null || !userStorage.existById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private void checkFilm(Long filmId) {
        if (filmId == null || !filmStorage.existsById(filmId)) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }
}
