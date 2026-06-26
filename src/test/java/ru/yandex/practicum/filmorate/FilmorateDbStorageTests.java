package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.db.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class,
        UserRowMapper.class, FilmRowMapper.class, GenreRowMapper.class, MpaRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateDbStorageTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;
    private final JdbcTemplate jdbc;

    private Boolean confirmed(long userId, long friendId) {
        return jdbc.queryForObject(
                "SELECT confirmed FROM friendships WHERE user_id = ? AND friend_id = ?",
                Boolean.class, userId, friendId);
    }

    private User newUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private Film newFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, null));
        return film;
    }

    // ---------- users ----------

    @Test
    void testCreateAndFindUser() {
        User created = userStorage.create(newUser("a@mail.ru", "alice"));

        assertThat(created.getId()).isNotNull();
        assertThat(userStorage.findById(created.getId()))
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("login", "alice");
    }

    @Test
    void testUpdateUser() {
        User created = userStorage.create(newUser("b@mail.ru", "bob"));
        created.setName("Bobby");
        userStorage.update(created);

        assertThat(userStorage.findById(created.getId()))
                .hasFieldOrPropertyWithValue("name", "Bobby");
    }

    @Test
    void testFindAllUsers() {
        userStorage.create(newUser("c@mail.ru", "carol"));
        userStorage.create(newUser("d@mail.ru", "dave"));

        assertThat(userStorage.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testExistById() {
        User created = userStorage.create(newUser("e@mail.ru", "eve"));

        assertThat(userStorage.existById(created.getId())).isTrue();
        assertThat(userStorage.existById(9999L)).isFalse();
    }

    @Test
    void testAddFriendIsOneSided() {
        User user = userStorage.create(newUser("u1@mail.ru", "u1"));
        User friend = userStorage.create(newUser("u2@mail.ru", "u2"));

        userStorage.addFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId()))
                .extracting(User::getId)
                .containsExactly(friend.getId());
        assertThat(userStorage.getFriends(friend.getId())).isEmpty();
    }

    @Test
    void testDeleteFriend() {
        User user = userStorage.create(newUser("u3@mail.ru", "u3"));
        User friend = userStorage.create(newUser("u4@mail.ru", "u4"));
        userStorage.addFriend(user.getId(), friend.getId());

        userStorage.deleteFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId())).isEmpty();
    }

    @Test
    void testFriendshipConfirmation() {
        User a = userStorage.create(newUser("fa@mail.ru", "fa"));
        User b = userStorage.create(newUser("fb@mail.ru", "fb"));

        // односторонняя заявка — неподтверждённая
        userStorage.addFriend(a.getId(), b.getId());
        assertThat(confirmed(a.getId(), b.getId())).isFalse();

        // ответная заявка — обе записи становятся подтверждёнными
        userStorage.addFriend(b.getId(), a.getId());
        assertThat(confirmed(a.getId(), b.getId())).isTrue();
        assertThat(confirmed(b.getId(), a.getId())).isTrue();

        // удаление дружбы возвращает ответную заявку в неподтверждённое состояние
        userStorage.deleteFriend(a.getId(), b.getId());
        assertThat(confirmed(b.getId(), a.getId())).isFalse();
    }

    @Test
    void testCommonFriends() {
        User user = userStorage.create(newUser("u5@mail.ru", "u5"));
        User other = userStorage.create(newUser("u6@mail.ru", "u6"));
        User common = userStorage.create(newUser("u7@mail.ru", "u7"));
        userStorage.addFriend(user.getId(), common.getId());
        userStorage.addFriend(other.getId(), common.getId());

        assertThat(userStorage.getCommonFriends(user.getId(), other.getId()))
                .extracting(User::getId)
                .containsExactly(common.getId());
    }

    // ---------- mpa ----------

    @Test
    void testFindAllMpa() {
        assertThat(mpaStorage.findAll()).hasSize(5);
    }

    @Test
    void testFindMpaById() {
        assertThat(mpaStorage.findById(1))
                .isPresent()
                .hasValueSatisfying(mpa -> assertThat(mpa.getName()).isEqualTo("G"));
        assertThat(mpaStorage.findById(999)).isEmpty();
    }

    @Test
    void testMpaExistsById() {
        assertThat(mpaStorage.existsById(1)).isTrue();
        assertThat(mpaStorage.existsById(999)).isFalse();
    }

    // ---------- genres ----------

    @Test
    void testFindAllGenres() {
        assertThat(genreStorage.findAll()).hasSize(6);
    }

    @Test
    void testFindGenreById() {
        assertThat(genreStorage.findById(1))
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre.getName()).isEqualTo("Комедия"));
        assertThat(genreStorage.findById(999)).isEmpty();
    }

    @Test
    void testGenreExistsById() {
        assertThat(genreStorage.existsById(1)).isTrue();
        assertThat(genreStorage.existsById(999)).isFalse();
    }

    // ---------- films ----------

    @Test
    void testFindAllFilms() {
        filmStorage.create(newFilm("Film one"));
        filmStorage.create(newFilm("Film two"));

        assertThat(filmStorage.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testCreateAndFindFilmWithMpaAndGenres() {
        Film film = newFilm("The Matrix");
        film.setGenres(new LinkedHashSet<>(List.of(new Genre(1, null), new Genre(2, null))));

        Film created = filmStorage.create(film);
        Film found = filmStorage.findById(created.getId());

        assertThat(found.getMpa()).hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G");
        assertThat(found.getGenres()).extracting(Genre::getId).containsExactly(1, 2);
    }

    @Test
    void testUpdateFilm() {
        Film created = filmStorage.create(newFilm("Old name"));
        created.setName("New name");
        created.setMpa(new Mpa(3, null));
        filmStorage.update(created);

        Film found = filmStorage.findById(created.getId());
        assertThat(found.getName()).isEqualTo("New name");
        assertThat(found.getMpa().getId()).isEqualTo(3);
    }

    @Test
    void testGetPopularOrdersByLikes() {
        Film unpopular = filmStorage.create(newFilm("Unpopular"));
        Film popular = filmStorage.create(newFilm("Popular"));
        User liker = userStorage.create(newUser("liker@mail.ru", "liker"));
        filmStorage.addLike(popular.getId(), liker.getId());

        List<Film> top = List.copyOf(filmStorage.getPopular(10));

        assertThat(top.get(0).getId()).isEqualTo(popular.getId());
    }

    @Test
    void testAddAndDeleteLike() {
        Film film = filmStorage.create(newFilm("Likeable"));
        User liker = userStorage.create(newUser("liker2@mail.ru", "liker2"));

        filmStorage.addLike(film.getId(), liker.getId());
        assertThat(List.copyOf(filmStorage.getPopular(1)).get(0).getId()).isEqualTo(film.getId());

        filmStorage.deleteLike(film.getId(), liker.getId());
        // после удаления лайка фильм всё ещё существует
        assertThat(filmStorage.existsById(film.getId())).isTrue();
    }
}
