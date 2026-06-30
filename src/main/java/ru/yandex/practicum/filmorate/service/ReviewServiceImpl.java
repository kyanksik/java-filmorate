package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
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
    private final EventService eventService;

    @Override
    public ReviewDto create(ReviewDto review) {
        checkUser(review.getUserId());
        checkFilm(review.getFilmId());
        Review created = reviewStorage.create(ReviewMapper.toModel(review));
        eventService.addEvent(created.getUserId(), EventType.REVIEW, Operation.ADD, created.getReviewId());
        return ReviewMapper.toDto(created);
    }

    @Override
    public ReviewDto update(ReviewDto review) {
        getOrThrow(review.getReviewId());
        Review updated = reviewStorage.update(ReviewMapper.toModel(review));
        eventService.addEvent(updated.getUserId(), EventType.REVIEW, Operation.UPDATE, updated.getReviewId());
        return ReviewMapper.toDto(updated);
    }

    @Override
    public void delete(long id) {
        Review review = getOrThrow(id);
        reviewStorage.delete(id);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.REMOVE, id);
    }

    @Override
    public ReviewDto findById(long id) {
        return ReviewMapper.toDto(getOrThrow(id));
    }

    @Override
    public Collection<ReviewDto> getByFilm(Long filmId, int count) {
        if (filmId != null) {
            checkFilm(filmId);
        }
        return reviewStorage.getByFilm(filmId, count).stream()
                .map(ReviewMapper::toDto)
                .toList();
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
