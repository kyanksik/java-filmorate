package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new ConcurrentHashMap<>();

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        // проверяем выполнение необходимых условий
        validate(film);
        // формируем дополнительные данные
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        // проверяем необходимые условия
        validate(newFilm);
        if (exists(newFilm)) {
            films.put(newFilm.getId(), newFilm);
            return newFilm;
        }
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @Override
    public Film findById(Long id) {
        if (!existsById(id)) {
            // Выбрасываем исключение, если ID нет в мапе
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        return films.get(id);
    }

    @Override
    public Collection<Film> getPopular(int count, Integer genreId, Integer year) {
        return findAll().stream()
                .filter(f -> genreId == null
                        || f.getGenres().stream().anyMatch(g -> g.getId().equals(genreId)))
                .filter(f -> year == null
                        || f.getReleaseDate() != null && f.getReleaseDate().getYear() == year)
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return films.containsKey(id);
    }

    @Override
    public Collection<Film> getByDirector(long directorId, String sortBy) {
        Comparator<Film> comparator = "likes".equalsIgnoreCase(sortBy)
                ? Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed()
                : Comparator.comparing(Film::getReleaseDate);
        return findAll().stream()
                .filter(f -> f.getDirectors().stream().anyMatch(d -> d.getId() == directorId))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Film> search(String query, String by) {
        String needle = query.toLowerCase();
        boolean byTitle = by.toLowerCase().contains("title");
        boolean byDirector = by.toLowerCase().contains("director");
        return findAll().stream()
                .filter(f -> byTitle && f.getName() != null && f.getName().toLowerCase().contains(needle)
                        || byDirector && f.getDirectors().stream()
                        .anyMatch(d -> d.getName() != null && d.getName().toLowerCase().contains(needle)))
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Film> getCommon(long userId, long friendId) {
        return findAll().stream()
                .filter(f -> f.getLikes().contains(userId) && f.getLikes().contains(friendId))
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Film> getRecommendations(long userId) {
        Set<Long> userLikes = findAll().stream()
                .filter(f -> f.getLikes().contains(userId))
                .map(Film::getId)
                .collect(Collectors.toSet());
        if (userLikes.isEmpty()) {
            return java.util.List.of();
        }
        Map<Long, Long> overlap = new HashMap<>();
        for (Film f : findAll()) {
            if (userLikes.contains(f.getId())) {
                for (Long other : f.getLikes()) {
                    if (other != userId) {
                        overlap.merge(other, 1L, Long::sum);
                    }
                }
            }
        }
        Optional<Long> bestUser = overlap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
        if (bestUser.isEmpty()) {
            return java.util.List.of();
        }
        long best = bestUser.get();
        return findAll().stream()
                .filter(f -> f.getLikes().contains(best) && !f.getLikes().contains(userId))
                .collect(Collectors.toList());
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        findById(filmId).getLikes().add(userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        findById(filmId).getLikes().remove(userId);
    }

    public static void validate(Film film) {
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private boolean exists(Film film) {
        return films.containsKey(film.getId());
    }

    // вспомогательный метод для генерации идентификатора
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}