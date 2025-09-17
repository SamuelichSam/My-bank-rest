package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequestDto;
import com.example.bankcards.dto.AuthResponseDto;
import com.example.bankcards.dto.RegisterRequestDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_ShouldRegisterNewUserSuccessfully() {
        RegisterRequestDto request = new RegisterRequestDto("testuser", "test@email.com", "password");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@email.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setUserRole(UserRole.USER);

        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(savedUser)).thenReturn("jwtToken");

        AuthResponseDto response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwtToken", response.token());
        assertEquals("testuser", response.username());
        assertEquals("USER", response.role());
        verify(userRepository).existsByEmail("test@email.com");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(savedUser);
    }

    @Test
    void register_ShouldThrowUserAlreadyExistsException_WhenEmailExists() {
        RegisterRequestDto request = new RegisterRequestDto("testuser", "existing@email.com", "password");
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(request)
        );

        assertEquals("Email already exists: existing@email.com", exception.getMessage());
        verify(userRepository).existsByEmail("existing@email.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_ShouldAuthenticateUserSuccessfully() {
        AuthRequestDto request = new AuthRequestDto("testuser", "password");
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@email.com");
        user.setPassword("encodedPassword");
        user.setUserRole(UserRole.USER);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("jwtToken");

        AuthResponseDto response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("jwtToken", response.token());
        assertEquals("testuser", response.username());
        assertEquals("USER", response.role());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password", "encodedPassword");
        verify(jwtUtil).generateToken(user);
    }

    @Test
    void authenticate_ShouldThrowBadCredentialsException_WhenUserNotFound() {
        AuthRequestDto request = new AuthRequestDto("nonexistent", "password");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticate(request)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void authenticate_ShouldThrowBadCredentialsException_WhenPasswordInvalid() {
        AuthRequestDto request = new AuthRequestDto("testuser", "wrongpassword");
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@email.com");
        user.setPassword("encodedPassword");
        user.setUserRole(UserRole.USER);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticate(request)
        );

        assertEquals("Invalid password", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void createAuthResponse_ShouldCreateCorrectResponse() {
        RegisterRequestDto request = new RegisterRequestDto("testuser", "test@email.com", "password");
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setUserRole(UserRole.ADMIN);

        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(user)).thenReturn("testToken");

        AuthResponseDto response = authService.register(request);

        assertNotNull(response);
        assertEquals("testToken", response.token());
        assertEquals("testuser", response.username());
        assertEquals("ADMIN", response.role());

        verify(userRepository).existsByEmail("test@email.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(user);
    }
}