package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;

public record UserUpdateDto(
        String username,
        String email,
        boolean enabled,
        UserRole userRole
) {}
