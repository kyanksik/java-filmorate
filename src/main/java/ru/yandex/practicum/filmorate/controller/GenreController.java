package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@Slf4j
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public Collection<GenreDto> findAll() {
        return genreService.findAll().stream()
                .map(GenreMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public GenreDto findById(@PathVariable int id) {
        return GenreMapper.toDto(genreService.findById(id));
    }
}
