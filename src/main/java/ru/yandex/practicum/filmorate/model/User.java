package ru.yandex.practicum.filmorate.model;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;


@Data

public class User {

    private Long id;

    @NotBlank(message = "Почта не может быть пустой")
    @Email(message = "Обязательно должна содержать @")
    private String email;

    @NotBlank(message = "Логин не может быть пустой")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения обязательна")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;


}
