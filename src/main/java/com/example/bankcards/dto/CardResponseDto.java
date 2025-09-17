package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponseDto(
        Long id,
        String maskedCardNumber,
        String cardHolderName,
        LocalDate expirationDate,
        CardStatus status,
        BigDecimal balance
) {

    public static CardResponseDto fromEntity(Card card) {
        return new CardResponseDto(
                card.getId(),
                maskCardNumber(card.getCardNumber()),
                card.getCardHolderName(),
                card.getExpirationDate(),
                card.getStatus(),
                card.getBalance()
        );
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            return "**** **** **** ****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
