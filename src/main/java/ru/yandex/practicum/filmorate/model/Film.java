package ru.yandex.practicum.filmorate.model;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import lombok.Data;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

@Data
public class Film {
    private int id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @PastOrPresent(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;

    public void setReleaseDate(LocalDate releaseDate) {
        LocalDate earliestDate = LocalDate.of(1895, 12, 28);
        if (releaseDate != null && releaseDate.isBefore(earliestDate)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        this.releaseDate = releaseDate;
    }
}