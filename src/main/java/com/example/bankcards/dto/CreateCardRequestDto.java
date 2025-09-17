package com.example.bankcards.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCardRequestDto(
        @NotBlank(message = "Номер карты обязателен")
        @Pattern(regexp = "^[0-9]{16,19}$", message = "Номер карты должен содержать от 16 до 19 цифр")
        String cardNumber,

        @NotBlank(message = "Имя владельца карты обязательно")
        @Size(min = 2, max = 100, message = "Имя владельца должно содержать от 2 до 100 символов")
        @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁ\\s]+$", message = "Имя владельца может содержать только буквы и пробелы")
        String cardHolderName,

        @NotNull(message = "Дата истечения срока обязательна")
        @Future(message = "Дата истечения срока должна быть в будущем")
        LocalDate expirationDate,

        @NotNull(message = "Начальный баланс обязателен")
        @DecimalMin(value = "0.0", inclusive = true, message = "Баланс не может быть отрицательным")
        @Digits(integer = 10, fraction = 2, message = "Баланс должен иметь не более 10 целых и 2 дробных цифр")
        BigDecimal initialBalance,

        @NotNull(message = "ID пользователя обязателен")
        @Positive(message = "ID пользователя должен быть положительным числом")
        Long userId
) {
}
