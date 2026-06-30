package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.dto.ReviewDto;

import java.util.Collection;

public interface ReviewService {

    ReviewDto create(ReviewDto review);

    ReviewDto update(ReviewDto review);

    void delete(long id);

    ReviewDto findById(long id);

    Collection<ReviewDto> getByFilm(Long filmId, int count);

    void addLike(long reviewId, long userId);

    void addDislike(long reviewId, long userId);

    void deleteLike(long reviewId, long userId);

    void deleteDislike(long reviewId, long userId);
}
