package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ReviewDto create(@Valid @RequestBody ReviewDto review) {
        return ReviewMapper.toDto(reviewService.create(ReviewMapper.toModel(review)));
    }

    @PutMapping
    public ReviewDto update(@Valid @RequestBody ReviewDto review) {
        return ReviewMapper.toDto(reviewService.update(ReviewMapper.toModel(review)));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public ReviewDto findById(@PathVariable long id) {
        return ReviewMapper.toDto(reviewService.findById(id));
    }

    @GetMapping
    public Collection<ReviewDto> getByFilm(@RequestParam(required = false) Long filmId,
                                           @RequestParam(defaultValue = "10") int count) {
        return reviewService.getByFilm(filmId, count).stream()
                .map(ReviewMapper::toDto)
                .toList();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable long id, @PathVariable long userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable long id, @PathVariable long userId) {
        reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable long id, @PathVariable long userId) {
        reviewService.deleteDislike(id, userId);
    }
}
