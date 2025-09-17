package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardEncryptionUtil encryptionUtil;

    @InjectMocks
    private CardServiceImpl cardService;

    private User testUser;
    private User adminUser;
    private Card testCard;
    private Card anotherCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserRole(UserRole.USER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUserRole(UserRole.ADMIN);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setCardNumber("encrypted123");
        testCard.setCardHolderName("John Doe");
        testCard.setExpirationDate(LocalDate.now().plusYears(1));
        testCard.setBalance(new BigDecimal("1000.00"));
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setUser(testUser);

        anotherCard = new Card();
        anotherCard.setId(2L);
        anotherCard.setCardNumber("encrypted456");
        anotherCard.setCardHolderName("Jane Smith");
        anotherCard.setExpirationDate(LocalDate.now().plusYears(2));
        anotherCard.setBalance(new BigDecimal("500.00"));
        anotherCard.setStatus(CardStatus.ACTIVE);
        anotherCard.setUser(testUser);
    }

    @Test
    void getUserCards_ShouldReturnUserCards() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(testCard, anotherCard)));

        // Act
        Page<CardResponseDto> result = cardService.getUserCards(1L, null, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getCardById_UserOwnsCard_ShouldReturnCard() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        CardResponseDto result = cardService.getCardById(1L, testUser);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.cardHolderName());
        verify(cardRepository).findById(1L);
    }

    @Test
    void getCardById_AdminAccess_ShouldReturnCard() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        CardResponseDto result = cardService.getCardById(1L, adminUser);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.cardHolderName());
        verify(cardRepository).findById(1L);
    }

    @Test
    void getCardById_UserNotOwner_ShouldThrowException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setUserRole(UserRole.USER);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThrows(OperationNotAllowedException.class,
                () -> cardService.getCardById(1L, otherUser));
        verify(cardRepository).findById(1L);
    }

    @Test
    void getCardById_CardNotFound_ShouldThrowException() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CardNotFoundException.class,
                () -> cardService.getCardById(1L, testUser));
        verify(cardRepository).findById(1L);
    }

    @Test
    void transferBetweenCards_ValidTransfer_ShouldSucceed() {
        // Arrange
        TransferRequestDto request = new TransferRequestDto(1L, 2L, new BigDecimal("100.00"), "desc");

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(anotherCard));

        // Act
        cardService.transferBetweenCards(request, testUser);

        // Assert
        assertEquals(new BigDecimal("900.00"), testCard.getBalance());
        assertEquals(new BigDecimal("600.00"), anotherCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferBetweenCards_InsufficientFunds_ShouldThrowException() {
        // Arrange
        TransferRequestDto request = new TransferRequestDto(1L, 2L, new BigDecimal("2000.00"), "desc");

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(anotherCard));

        // Act & Assert
        assertThrows(InsufficientFundsException.class,
                () -> cardService.transferBetweenCards(request, testUser));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenCards_CardNotFound_ShouldThrowException() {
        // Arrange
        TransferRequestDto request = new TransferRequestDto(1L, 2L, new BigDecimal("2000.00"), "desc");

        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CardNotFoundException.class,
                () -> cardService.transferBetweenCards(request, testUser));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenCards_NotUserCards_ShouldThrowException() {
        // Arrange
        TransferRequestDto request = new TransferRequestDto(1L, 2L, new BigDecimal("2000.00"), "desc");
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setUserRole(UserRole.USER);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(anotherCard));

        // Act & Assert
        assertThrows(OperationNotAllowedException.class,
                () -> cardService.transferBetweenCards(request, otherUser));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void getCardBalance_UserOwnsCard_ShouldReturnBalance() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        BigDecimal result = cardService.getCardBalance(1L, testUser);

        // Assert
        assertEquals(new BigDecimal("1000.00"), result);
        verify(cardRepository).findById(1L);
    }

    @Test
    void requestBlockCard_ValidRequest_ShouldBlockCard() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        cardService.requestBlockCard(1L, testUser);

        // Assert
        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
        verify(cardRepository).save(testCard);
    }

    @Test
    void requestBlockCard_AlreadyBlocked_ShouldThrowException() {
        // Arrange
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThrows(OperationNotAllowedException.class,
                () -> cardService.requestBlockCard(1L, testUser));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void getAllCards_ShouldReturnAllCards() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(testCard, anotherCard)));

        // Act
        Page<CardResponseDto> result = cardService.getAllCards(null, null, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void createCard_ValidRequest_ShouldCreateCard() {
        // Arrange
        CreateCardRequestDto request = new CreateCardRequestDto("1234567890123456", "John Doe",
                LocalDate.now().plusYears(1), new BigDecimal("1000.00"), 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(encryptionUtil.encrypt("1234567890123456")).thenReturn("encrypted123");
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        CardResponseDto result = cardService.createCard(request);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(encryptionUtil).encrypt("1234567890123456");
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_UserNotFound_ShouldThrowException() {
        // Arrange
        CreateCardRequestDto request = new CreateCardRequestDto("1234567890123456", "John Doe",
                LocalDate.now().plusYears(1), new BigDecimal("1000.00"), 99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> cardService.createCard(request));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void updateCardStatus_ValidUpdate_ShouldUpdateStatus() {
        // Arrange
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        CardResponseDto result = cardService.updateCardStatus(1L, CardStatus.ACTIVE);

        // Assert
        assertNotNull(result);
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void updateCardStatus_ActivateExpiredCard_ShouldThrowException() {
        // Arrange
        testCard.setExpirationDate(LocalDate.now().minusDays(1));
        testCard.updateStatus(); // This should mark it as expired
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThrows(OperationNotAllowedException.class,
                () -> cardService.updateCardStatus(1L, CardStatus.ACTIVE));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void deleteCard_ValidDelete_ShouldDeleteCard() {
        // Arrange
        testCard.setBalance(BigDecimal.ZERO);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        doNothing().when(cardRepository).delete(testCard);

        // Act
        cardService.deleteCard(1L);

        // Assert
        verify(cardRepository).findById(1L);
        verify(cardRepository).delete(testCard);
    }

    @Test
    void deleteCard_PositiveBalance_ShouldThrowException() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThrows(OperationNotAllowedException.class,
                () -> cardService.deleteCard(1L));
        verify(cardRepository, never()).delete(any(Card.class));
    }

    @Test
    void deleteCard_CardNotFound_ShouldThrowException() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CardNotFoundException.class,
                () -> cardService.deleteCard(1L));
        verify(cardRepository, never()).delete(any(Card.class));
    }
}