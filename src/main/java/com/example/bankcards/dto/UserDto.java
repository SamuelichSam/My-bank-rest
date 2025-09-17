package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;

import java.util.List;

public record UserDto(
        Long id,
        String username,
        String email,
        boolean enabled,
        UserRole userRole,
        List<CardAdminResponseDto> cards
) {

    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getUserRole(),
                user.getCards() != null ?
                        user.getCards().stream()
                                .map(CardAdminResponseDto::fromEntity)
                                .toList()
                        : List.of()
        );
    }
}
