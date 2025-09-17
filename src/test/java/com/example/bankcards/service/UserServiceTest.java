package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setEnabled(true);
        testUser.setUserRole(UserRole.USER);
    }

    @Test
    void updateUser_ValidUpdate_ShouldUpdateUser() {
        // Arrange
        UserUpdateDto updateDto = new UserUpdateDto("newusername", "new@example.com", true, UserRole.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDto result = userService.updateUser(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsername("newusername");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_DuplicateUsername_ShouldThrowException() {
        // Arrange
        UserUpdateDto updateDto = new UserUpdateDto("existinguser", "test@example.com", true, UserRole.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateUsernameException.class,
                () -> userService.updateUser(1L, updateDto));
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateUser_DuplicateEmail_ShouldThrowException() {
        // Arrange
        UserUpdateDto updateDto = new UserUpdateDto("testuser", "existing@example.com", true, UserRole.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmailException.class,
                () -> userService.updateUser(1L, updateDto));
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).existsByUsername(anyString());
    }

    @Test
    void updateUser_UserNotFound_ShouldThrowException() {
        // Arrange
        UserUpdateDto updateDto = new UserUpdateDto("newusername", "new@example.com", true, UserRole.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(1L, updateDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ValidDelete_ShouldDeleteUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.existsByUserId(1L)).thenReturn(false);
        doNothing().when(userRepository).delete(testUser);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).findById(1L);
        verify(cardRepository).existsByUserId(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_UserHasCards_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.existsByUserId(1L)).thenReturn(true);

        // Act & Assert
        assertThrows(UserHasCardsException.class,
                () -> userService.deleteUser(1L));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_UserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(1L));
        verify(cardRepository, never()).existsByUserId(anyLong());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getAllUsers_WithSearch_ShouldReturnFilteredUsers() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        when(userRepository.findBySearchWithCards("test", pageable))
                .thenReturn(new PageImpl<>(List.of(testUser)));

        // Act
        Page<UserDto> result = userService.getAllUsers("test", pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findBySearchWithCards("test", pageable);
    }

    @Test
    void getAllUsers_WithoutSearch_ShouldReturnAllUsers() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        when(userRepository.findAllUsersWithCards(pageable))
                .thenReturn(new PageImpl<>(List.of(testUser)));

        // Act
        Page<UserDto> result = userService.getAllUsers(null, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAllUsersWithCards(pageable);
    }

    @Test
    void blockUser_ValidBlock_ShouldDisableUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.blockUser(1L);

        // Assert
        assertFalse(testUser.isEnabled());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void blockUser_UserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userService.blockUser(1L));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unblockUser_ValidUnblock_ShouldEnableUser() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.unblockUser(1L);

        // Assert
        assertTrue(testUser.isEnabled());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void unblockUser_UserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userService.unblockUser(1L));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ValidUser_ShouldReturnUser() {
        // Arrange
        when(userRepository.findWithCardsById(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserDto result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.username());
        verify(userRepository).findWithCardsById(1L);
    }

    @Test
    void getUserById_UserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findWithCardsById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(1L));
        verify(userRepository).findWithCardsById(1L);
    }

    @Test
    void existsByUsername_UsernameExists_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean result = userService.existsByUsername("testuser");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void existsByUsername_UsernameNotExists_ShouldReturnFalse() {
        // Arrange
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // Act
        boolean result = userService.existsByUsername("nonexistent");

        // Assert
        assertFalse(result);
        verify(userRepository).existsByUsername("nonexistent");
    }

    @Test
    void existsByEmail_EmailExists_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean result = userService.existsByEmail("test@example.com");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void existsByEmail_EmailNotExists_ShouldReturnFalse() {
        // Arrange
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // Act
        boolean result = userService.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result);
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }

    @Test
    void loadUserByUsername_ValidUser_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_UserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonexistent"));
        verify(userRepository).findByUsername("nonexistent");
    }
}