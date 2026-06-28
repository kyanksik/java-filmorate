package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.db.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.db.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.db.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.db.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.EventRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class,
        DirectorDbStorage.class, ReviewDbStorage.class, EventDbStorage.class,
        UserRowMapper.class, FilmRowMapper.class, GenreRowMapper.class, MpaRowMapper.class,
        DirectorRowMapper.class, ReviewRowMapper.class, EventRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateDbStorageTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;
    private final DirectorDbStorage directorStorage;
    private final ReviewDbStorage reviewStorage;
    private final EventDbStorage eventStorage;
    private final JdbcTemplate jdbc;

    private Review newReview(long userId, long filmId, boolean positive) {
        Review review = new Review();
        review.setContent("review content");
        review.setIsPositive(positive);
        review.setUserId(userId);
        review.setFilmId(filmId);
        return review;
    }

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

        List<Film> top = List.copyOf(filmStorage.getPopular(10, null, null));

        assertThat(top.get(0).getId()).isEqualTo(popular.getId());
    }

    @Test
    void testGetPopularWithGenreAndYearFilters() {
        Film film = newFilm("Filtered");
        film.setReleaseDate(LocalDate.of(1999, 5, 5));
        film.setGenres(new LinkedHashSet<>(List.of(new Genre(1, null))));
        film = filmStorage.create(film);

        assertThat(filmStorage.getPopular(10, 1, 1999))
                .extracting(Film::getId).contains(film.getId());
        assertThat(filmStorage.getPopular(10, 2, null))
                .extracting(Film::getId).doesNotContain(film.getId());
        assertThat(filmStorage.getPopular(10, null, 1888))
                .extracting(Film::getId).doesNotContain(film.getId());
    }

    @Test
    void testAddAndDeleteLike() {
        Film film = filmStorage.create(newFilm("Likeable"));
        User liker = userStorage.create(newUser("liker2@mail.ru", "liker2"));

        filmStorage.addLike(film.getId(), liker.getId());
        assertThat(List.copyOf(filmStorage.getPopular(1, null, null)).get(0).getId()).isEqualTo(film.getId());

        filmStorage.deleteLike(film.getId(), liker.getId());
        // после удаления лайка фильм всё ещё существует
        assertThat(filmStorage.existsById(film.getId())).isTrue();
    }

    // ---------- directors ----------

    @Test
    void testCreateUpdateFindDeleteDirector() {
        Director created = directorStorage.create(new Director(null, "Nolan"));
        assertThat(created.getId()).isNotNull();
        assertThat(directorStorage.findById(created.getId()))
                .hasValueSatisfying(d -> assertThat(d.getName()).isEqualTo("Nolan"));

        created.setName("Christopher Nolan");
        directorStorage.update(created);
        assertThat(directorStorage.findById(created.getId()))
                .hasValueSatisfying(d -> assertThat(d.getName()).isEqualTo("Christopher Nolan"));

        assertThat(directorStorage.existsById(created.getId())).isTrue();
        directorStorage.delete(created.getId());
        assertThat(directorStorage.existsById(created.getId())).isFalse();
    }

    @Test
    void testFilmKeepsDirectorAndGetByDirectorSorted() {
        Director director = directorStorage.create(new Director(null, "Tarantino"));

        Film older = newFilm("Older");
        older.setReleaseDate(LocalDate.of(2000, 1, 1));
        older.setDirectors(new LinkedHashSet<>(List.of(new Director(director.getId(), null))));
        older = filmStorage.create(older);

        Film newer = newFilm("Newer");
        newer.setReleaseDate(LocalDate.of(2010, 1, 1));
        newer.setDirectors(new LinkedHashSet<>(List.of(new Director(director.getId(), null))));
        newer = filmStorage.create(newer);

        // фильм хранит режиссёра с именем
        assertThat(filmStorage.findById(older.getId()).getDirectors())
                .extracting(Director::getName)
                .containsExactly("Tarantino");

        // сортировка по году — по возрастанию
        List<Film> byYear = List.copyOf(filmStorage.getByDirector(director.getId(), "year"));
        assertThat(byYear).extracting(Film::getId).containsExactly(older.getId(), newer.getId());

        // сортировка по лайкам — по убыванию
        User liker = userStorage.create(newUser("dlike@mail.ru", "dlike"));
        filmStorage.addLike(newer.getId(), liker.getId());
        List<Film> byLikes = List.copyOf(filmStorage.getByDirector(director.getId(), "likes"));
        assertThat(byLikes.get(0).getId()).isEqualTo(newer.getId());
    }

    @Test
    void testSearchByTitleAndDirector() {
        Director director = directorStorage.create(new Director(null, "xyzdir"));

        Film byTitle = newFilm("xyzfilm");
        byTitle = filmStorage.create(byTitle);

        Film byDir = newFilm("Other");
        byDir.setDirectors(new LinkedHashSet<>(List.of(new Director(director.getId(), null))));
        byDir = filmStorage.create(byDir);

        assertThat(filmStorage.search("xyz", "title"))
                .extracting(Film::getId).containsExactly(byTitle.getId());
        assertThat(filmStorage.search("xyz", "director"))
                .extracting(Film::getId).containsExactly(byDir.getId());
        assertThat(filmStorage.search("xyz", "title,director"))
                .extracting(Film::getId).containsExactlyInAnyOrder(byTitle.getId(), byDir.getId());
        assertThat(filmStorage.search("notfoundzzz", "title,director")).isEmpty();
    }

    @Test
    void testGetCommonFilms() {
        User u1 = userStorage.create(newUser("cu1@mail.ru", "cu1"));
        User u2 = userStorage.create(newUser("cu2@mail.ru", "cu2"));
        Film common = filmStorage.create(newFilm("Common"));
        Film onlyOne = filmStorage.create(newFilm("OnlyOne"));
        filmStorage.addLike(common.getId(), u1.getId());
        filmStorage.addLike(common.getId(), u2.getId());
        filmStorage.addLike(onlyOne.getId(), u1.getId());

        assertThat(filmStorage.getCommon(u1.getId(), u2.getId()))
                .extracting(Film::getId).containsExactly(common.getId());
    }

    // ---------- reviews ----------

    @Test
    void testReviewCrudAndUseful() {
        User author = userStorage.create(newUser("ra@mail.ru", "ra"));
        Film film = filmStorage.create(newFilm("Reviewed"));

        Review created = reviewStorage.create(newReview(author.getId(), film.getId(), false));
        assertThat(created.getReviewId()).isNotNull();
        assertThat(created.getUseful()).isZero();
        // у свежего отзыва без реакций useful должен читаться как 0
        assertThat(reviewStorage.findById(created.getReviewId()).orElseThrow().getUseful()).isZero();

        // update меняет content/isPositive, useful остаётся вычисляемым
        created.setContent("updated");
        created.setIsPositive(true);
        Review updated = reviewStorage.update(created);
        assertThat(updated.getContent()).isEqualTo("updated");
        assertThat(updated.getIsPositive()).isTrue();

        // лайк/дизлайк меняют useful
        User u1 = userStorage.create(newUser("ru1@mail.ru", "ru1"));
        User u2 = userStorage.create(newUser("ru2@mail.ru", "ru2"));
        reviewStorage.addReaction(created.getReviewId(), u1.getId(), true);
        assertThat(reviewStorage.findById(created.getReviewId()).orElseThrow().getUseful()).isEqualTo(1);
        reviewStorage.addReaction(created.getReviewId(), u2.getId(), false);
        assertThat(reviewStorage.findById(created.getReviewId()).orElseThrow().getUseful()).isZero();
        reviewStorage.removeReaction(created.getReviewId(), u1.getId());
        assertThat(reviewStorage.findById(created.getReviewId()).orElseThrow().getUseful()).isEqualTo(-1);

        // выборка по фильму и удаление
        assertThat(reviewStorage.getByFilm(film.getId(), 10)).hasSize(1);
        reviewStorage.delete(created.getReviewId());
        assertThat(reviewStorage.existsById(created.getReviewId())).isFalse();
    }

    // ---------- feed ----------

    @Test
    void testEventFeedInChronologicalOrder() {
        User user = userStorage.create(newUser("feed@mail.ru", "feeduser"));

        Event first = new Event();
        first.setUserId(user.getId());
        first.setEventType(EventType.FRIEND);
        first.setOperation(Operation.ADD);
        first.setEntityId(42L);
        first.setTimestamp(1000L);
        eventStorage.add(first);

        Event second = new Event();
        second.setUserId(user.getId());
        second.setEventType(EventType.LIKE);
        second.setOperation(Operation.ADD);
        second.setEntityId(7L);
        second.setTimestamp(2000L);
        eventStorage.add(second);

        List<Event> feed = List.copyOf(eventStorage.getByUser(user.getId()));
        assertThat(feed).hasSize(2);
        assertThat(feed.get(0).getEventType()).isEqualTo(EventType.FRIEND);
        assertThat(feed.get(0).getEntityId()).isEqualTo(42L);
        assertThat(feed.get(1).getEventType()).isEqualTo(EventType.LIKE);
        assertThat(feed.get(1).getEventId()).isNotNull();
    }
}
