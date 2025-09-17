package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.AdminCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Cards", description = "API для административного управления картами")
public class AdminCardController {

    private final AdminCardService cardService;

    @GetMapping
    @Operation(summary = "Получить все карты (только для ADMIN)")
    public ResponseEntity<Page<CardResponseDto>> getAllCards(@Valid
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CardStatus status) {
        return ResponseEntity.ok(cardService.getAllCards(search, status, pageable));
    }

    @PostMapping
    @Operation(summary = "Создать новую карту (только для ADMIN)")
    public ResponseEntity<CardResponseDto> createCard(@Valid
            @RequestBody CreateCardRequestDto request) {
        return ResponseEntity.ok(cardService.createCard(request));
    }

    @PostMapping("/{cardId}/status")
    @Operation(summary = "Изменить статус карты ( только для ADMIN )")
    public ResponseEntity<CardResponseDto> updateCardStatus(@Valid
            @PathVariable Long cardId,
            @RequestParam CardStatus status) {
        return ResponseEntity.ok(cardService.updateCardStatus(cardId, status));
    }

    @DeleteMapping("/{cardId}")
    @Operation(summary = "Удалить карту ( только для ADMIN")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}

