package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import static org.junit.jupiter.api.Assertions.*;

public class FilmTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsTooEarly() {
        Film film = new Film();
        film.setName("Старое кино");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));


        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validate(film);
        });

        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void shouldSuccessWhenFilmIsCorrect() {
        Film film = new Film();
        film.setName("Начало");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2010, 7, 8));
        film.setDuration(148);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Валидация должна проходить для корректных данных");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = new Film();
        film.setName(""); // Пустое имя
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Валидация должна найти ошибку в пустом имени");
    }

    @Test
    void shouldFailWhenDescriptionIsTooLong() {
        Film film = new Film();
        film.setName("Фильм");
        // Создаем строку ровно в 201 символ (граница 200)
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Описание более 200 символов должно быть отклонено");
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Фильм");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(-1); // Отрицательная длительность

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Отрицательная продолжительность недопустима");
    }

    @Test
    void shouldFailWhenDurationIsZero() {
        Film film = new Film();
        film.setName("Фильм");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(0); // Ноль (аннотация @Positive не пропустит 0)

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Нулевая продолжительность должна быть отклонена");
    }

    private void validate(Film film) {
        InMemoryFilmStorage.validate(film);
    }
}
