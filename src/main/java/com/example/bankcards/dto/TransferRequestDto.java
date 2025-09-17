package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferRequestDto(
        @NotNull(message = "ID карты отправителя обязателен")
        Long fromCardId,

        @NotNull(message = "ID карты получателя обязателен")
        Long toCardId,

        @NotNull(message = "Сумма перевода обязательна")
        @DecimalMin(value = "0.01", message = "Сумма перевода должна быть не менее 0.01")
        BigDecimal amount,

        @NotBlank(message = "Описание перевода обязательно")
        @Size(max = 255, message = "Описание не должно превышать 255 символов")
        String description
) {
}
