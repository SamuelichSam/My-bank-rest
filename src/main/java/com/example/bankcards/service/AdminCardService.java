package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCardService {
    Page<CardResponseDto> getAllCards(String search, CardStatus status, Pageable pageable);

    CardResponseDto createCard(CreateCardRequestDto request);

    CardResponseDto updateCardStatus(Long cardId, CardStatus status);

    void deleteCard(Long cardId);
}
