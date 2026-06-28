package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.service.FilmService;


import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<FilmDto> findAll() {
        return filmService.findAll().stream()
                .map(FilmMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public FilmDto findById(@PathVariable Long id) {
        return FilmMapper.toDto(filmService.findById(id));
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody FilmDto film) {
        return FilmMapper.toDto(filmService.create(FilmMapper.toModel(film)));
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody FilmDto newFilm) {
        return FilmMapper.toDto(filmService.update(FilmMapper.toModel(newFilm)));
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> getPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopular(count).stream()
                .map(FilmMapper::toDto)
                .toList();
    }

    @GetMapping("/common")
    public Collection<FilmDto> getCommon(@RequestParam long userId, @RequestParam long friendId) {
        return filmService.getCommon(userId, friendId).stream()
                .map(FilmMapper::toDto)
                .toList();
    }

    @GetMapping("/search")
    public Collection<FilmDto> search(@RequestParam String query, @RequestParam String by) {
        return filmService.search(query, by).stream()
                .map(FilmMapper::toDto)
                .toList();
    }

    @GetMapping("/director/{directorId}")
    public Collection<FilmDto> getByDirector(@PathVariable long directorId,
                                             @RequestParam(defaultValue = "likes") String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy).stream()
                .map(FilmMapper::toDto)
                .toList();
    }

}
