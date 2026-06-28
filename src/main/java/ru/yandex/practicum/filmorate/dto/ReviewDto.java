package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewDto {

    private Long reviewId;

    @NotBlank(message = "Содержание отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва (isPositive) обязателен")
    @JsonProperty("isPositive")
    private Boolean isPositive;

    @NotNull(message = "Идентификатор пользователя обязателен")
    private Long userId;

    @NotNull(message = "Идентификатор фильма обязателен")
    private Long filmId;

    private int useful;
}
