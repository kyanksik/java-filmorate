package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public Collection<MpaDto> findAll() {
        return mpaService.findAll().stream()
                .map(MpaMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public MpaDto findById(@PathVariable int id) {
        return MpaMapper.toDto(mpaService.findById(id));
    }
}
