package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;

import java.time.LocalDate;

public record CardAdminResponseDto(
        Long id,
        String maskedCardNumber,
        String cardHolderName,
        LocalDate expirationDate,
        CardStatus status
) {

    public static CardAdminResponseDto fromEntity(Card card) {
        return new CardAdminResponseDto(
                card.getId(),
                maskCardNumber(card.getCardNumber()),
                card.getCardHolderName(),
                card.getExpirationDate(),
                card.getStatus()
        );
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            return "**** **** **** ****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
