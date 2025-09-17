package com.example.bankcards.dto;

public record AuthResponseDto(
        String token,
        String username,
        String role
) {
}
