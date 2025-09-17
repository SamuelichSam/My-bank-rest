package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Users", description = "API для административного управления пользователями")
public class AdminUserController {

    private final UserService userService;

    @PostMapping("/{userId}")
    @Operation(summary = "Изменить пользователя ( только для ADMIN )")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId,
                                              @RequestBody UserUpdateDto userDto) {
        return ResponseEntity.ok(userService.updateUser(userId, userDto));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Удалить пользователя ( только для ADMIN )")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Получить пользователя по id ( только для ADMIN )")
    private ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }


    @GetMapping
    @Operation(summary = "Получить всех пользователей ( только для ADMIN )")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(userService.getAllUsers(search, pageable));
    }

    @PatchMapping("/{userId}/block")
    @Operation(summary = "Заблокировать пользователя ( только для ADMIN )")
    public ResponseEntity<Void> blockUser(@PathVariable Long userId) {
        userService.blockUser(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/unblock")
    @Operation(summary = "Разблокировать пользователя ( только для ADMIN )")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId) {
        userService.unblockUser(userId);
        return ResponseEntity.ok().build();
    }
}
