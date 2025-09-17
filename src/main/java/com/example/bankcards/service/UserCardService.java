package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface UserCardService {
    Page<CardResponseDto> getUserCards(Long userId, String search, Pageable pageable);

    CardResponseDto getCardById(Long cardId, User user);

    void transferBetweenCards(TransferRequestDto request, User user);

    BigDecimal getCardBalance(Long cardId, User user);

    void requestBlockCard(Long cardId, User user);
}
