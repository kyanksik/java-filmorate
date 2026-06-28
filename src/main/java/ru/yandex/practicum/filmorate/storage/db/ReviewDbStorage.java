package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private static final String BASE_SELECT = """
            SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id,
                   COALESCE(SUM(CASE WHEN rl.is_useful THEN 1 ELSE -1 END), 0) AS useful
            FROM reviews r
            LEFT JOIN review_likes rl ON r.review_id = rl.review_id
            """;

    private static final String GROUP_BY =
            " GROUP BY r.review_id, r.content, r.is_positive, r.user_id, r.film_id ";

    private final JdbcTemplate jdbc;
    private final RowMapper<Review> reviewRowMapper;

    @Override
    public Review create(Review review) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");
        Number id = insert.executeAndReturnKey(Map.of(
                "content", review.getContent(),
                "is_positive", review.getIsPositive(),
                "user_id", review.getUserId(),
                "film_id", review.getFilmId()
        ));
        review.setReviewId(id.longValue());
        review.setUseful(0);
        return review;
    }

    @Override
    public Review update(Review review) {
        jdbc.update("UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?",
                review.getContent(), review.getIsPositive(), review.getReviewId());
        return findById(review.getReviewId()).orElseThrow();
    }

    @Override
    public void delete(long id) {
        jdbc.update("DELETE FROM reviews WHERE review_id = ?", id);
    }

    @Override
    public Optional<Review> findById(long id) {
        return jdbc.query(BASE_SELECT + " WHERE r.review_id = ?" + GROUP_BY, reviewRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsById(long id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM reviews WHERE review_id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Collection<Review> getByFilm(Long filmId, int count) {
        if (filmId == null) {
            return jdbc.query(BASE_SELECT + GROUP_BY + " ORDER BY useful DESC LIMIT ?",
                    reviewRowMapper, count);
        }
        return jdbc.query(BASE_SELECT + " WHERE r.film_id = ?" + GROUP_BY + " ORDER BY useful DESC LIMIT ?",
                reviewRowMapper, filmId, count);
    }

    @Override
    public void addReaction(long reviewId, long userId, boolean useful) {
        jdbc.update("MERGE INTO review_likes (review_id, user_id, is_useful) KEY (review_id, user_id) VALUES (?, ?, ?)",
                reviewId, userId, useful);
    }

    @Override
    public void removeReaction(long reviewId, long userId) {
        jdbc.update("DELETE FROM review_likes WHERE review_id = ? AND user_id = ?", reviewId, userId);
    }
}
