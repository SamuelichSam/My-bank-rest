package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cards", description = "API для управления банковскими картами")
public class UserCardController {

    private final UserCardService cardService;

    @GetMapping("/my")
    @Operation(summary = "Получить свои карты")
    public ResponseEntity<Page<CardResponseDto>> getCards(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(cardService.getUserCards(user.getId(), search, pageable));
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Получить карту по ID")
    public ResponseEntity<CardResponseDto> getCard(
            @PathVariable Long cardId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cardService.getCardById(cardId, user));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Перевод между своими картами")
    public ResponseEntity<Void> transferBetweenCards(@Valid
                                                     @RequestBody TransferRequestDto transferRequestDto,
                                                     @AuthenticationPrincipal User user) {
        cardService.transferBetweenCards(transferRequestDto, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{cardId}/balance")
    @Operation(summary = "Получить баланс карты")
    public ResponseEntity<BigDecimal> getCardBalance(
            @PathVariable Long cardId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cardService.getCardBalance(cardId, user));
    }

    @PostMapping("/{cardId}/block-request")
    @Operation(summary = "Запрос на блокировку карты")
    public ResponseEntity<Void> requestBlockCard(
            @PathVariable Long cardId,
            @AuthenticationPrincipal User user) {
        cardService.requestBlockCard(cardId, user);
        return ResponseEntity.ok().build();
    }
}
