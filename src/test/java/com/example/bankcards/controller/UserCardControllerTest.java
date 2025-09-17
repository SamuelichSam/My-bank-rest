package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.service.UserCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserCardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserCardService cardService;

    @InjectMocks
    private UserCardController userCardController;

    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setFallbackPageable(PageRequest.of(0, 10));

        mockMvc = MockMvcBuilders.standaloneSetup(userCardController)
                .setCustomArgumentResolvers(pageableResolver)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setUserRole(UserRole.USER);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getCard_ValidCardId_ShouldReturnCard() throws Exception {
        // Arrange
        CardResponseDto cardResponse = new CardResponseDto(
                1L, "************7890", "John Doe",
                LocalDate.now().plusYears(1), CardStatus.ACTIVE, new BigDecimal("1000.00")
        );

        when(cardService.getCardById(eq(1L), any(User.class))).thenReturn(cardResponse);

        // Act & Assert
        mockMvc.perform(get("/cards/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cardHolderName").value("John Doe"));

        verify(cardService).getCardById(eq(1L), any(User.class));
    }

    @Test
    void getCard_InvalidCardId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/cards/invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferBetweenCards_ValidRequest_ShouldTransfer() throws Exception {
        // Arrange
        TransferRequestDto transferRequest = new TransferRequestDto(
                1L, 2L, new BigDecimal("100.00"), "description");
        doNothing().when(cardService).transferBetweenCards(any(TransferRequestDto.class), any(User.class));

        // Act & Assert
        mockMvc.perform(post("/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk());

        verify(cardService).transferBetweenCards(any(TransferRequestDto.class), any(User.class));
    }

    @Test
    void transferBetweenCards_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange - невалидный запрос
        TransferRequestDto invalidRequest = new TransferRequestDto(null, null, null, null);

        // Act & Assert
        mockMvc.perform(post("/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardBalance_ValidCardId_ShouldReturnBalance() throws Exception {
        // Arrange
        when(cardService.getCardBalance(eq(1L), any(User.class))).thenReturn(new BigDecimal("1000.00"));

        // Act & Assert
        mockMvc.perform(get("/cards/1/balance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.00"));

        verify(cardService).getCardBalance(eq(1L), any(User.class));
    }

    @Test
    void getCardBalance_InvalidCardId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/cards/invalid/balance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestBlockCard_ValidCardId_ShouldRequestBlock() throws Exception {
        // Arrange
        doNothing().when(cardService).requestBlockCard(eq(1L), any(User.class));

        // Act & Assert
        mockMvc.perform(post("/cards/1/block-request")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cardService).requestBlockCard(eq(1L), any(User.class));
    }

    @Test
    void requestBlockCard_InvalidCardId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/cards/invalid/block-request")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferBetweenCards_MissingRequestBody_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferBetweenCards_InvalidContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        // Arrange
        TransferRequestDto transferRequest = new TransferRequestDto(
                1L, 2L, new BigDecimal("100.00"), "description");

        // Act & Assert
        mockMvc.perform(post("/cards/transfer")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }
}