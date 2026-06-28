package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@Slf4j
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<DirectorDto> findAll() {
        return directorService.findAll().stream()
                .map(DirectorMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public DirectorDto findById(@PathVariable long id) {
        return DirectorMapper.toDto(directorService.findById(id));
    }

    @PostMapping
    public DirectorDto create(@Valid @RequestBody DirectorDto director) {
        return DirectorMapper.toDto(directorService.create(DirectorMapper.toModel(director)));
    }

    @PutMapping
    public DirectorDto update(@Valid @RequestBody DirectorDto director) {
        return DirectorMapper.toDto(directorService.update(DirectorMapper.toModel(director)));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        directorService.delete(id);
    }
}
