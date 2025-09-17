package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.service.AdminCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminCardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminCardService cardService;

    @InjectMocks
    private AdminCardController adminCardController;

    private ObjectMapper objectMapper;
    private User adminUser;

    @BeforeEach
    void setUp() {
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setFallbackPageable(PageRequest.of(0, 20));

        mockMvc = MockMvcBuilders.standaloneSetup(adminCardController)
                .setCustomArgumentResolvers(pageableResolver)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.registerModule(new SpringDataJacksonConfiguration.PageModule());


        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setUserRole(UserRole.ADMIN);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                adminUser, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getAllCards_WithSearchAndStatus_ShouldReturnCards() throws Exception {
        CardResponseDto cardResponse = new CardResponseDto(
                1L, "************7890", "John Doe",
                LocalDate.now().plusYears(1), CardStatus.ACTIVE, new BigDecimal("1000.00")
        );

        Page<CardResponseDto> page = new PageImpl<>(List.of(cardResponse), PageRequest.of(0, 20), 1);

        when(cardService.getAllCards(eq("test"), eq(CardStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

        MvcResult result = mockMvc.perform(get("/admin/cards")
                        .param("search", "test")
                        .param("status", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);

        mockMvc.perform(get("/admin/cards")
                        .param("search", "test")
                        .param("status", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").exists()); // просто проверяем что есть content
    }

    @Test
    void getAllCards_WithoutParams_ShouldReturnCards() throws Exception {
        CardResponseDto cardResponse = new CardResponseDto(
                1L, "************7890", "John Doe",
                LocalDate.now().plusYears(1), CardStatus.ACTIVE, new BigDecimal("1000.00"));

        Page<CardResponseDto> page = new PageImpl<>(List.of(cardResponse), PageRequest.of(0, 20), 1);

        when(cardService.getAllCards(eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cardService).getAllCards(eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void createCard_ValidRequest_ShouldCreateCard() throws Exception {
        CreateCardRequestDto request = new CreateCardRequestDto(
                "1234567890123456",
                "John Doe",
                LocalDate.now().plusYears(1),
                new BigDecimal("1000.00"),
                1L);

        CardResponseDto response = new CardResponseDto(
                1L,
                "************3456",
                "John Doe",
                LocalDate.now().plusYears(1),
                CardStatus.ACTIVE,
                new BigDecimal("1000.00")
        );

        when(cardService.createCard(any(CreateCardRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cardHolderName").value("John Doe"))
                .andExpect(jsonPath("$.maskedCardNumber").value("************3456"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    void createCard_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        CreateCardRequestDto invalidRequest = new CreateCardRequestDto(
                null, null, null, null, null
        );

        mockMvc.perform(post("/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCardStatus_FromActiveToBlocked_ShouldReturnBlockedStatus() throws Exception {
        CardResponseDto response = new CardResponseDto(
                1L, "************7890", "John Doe",
                LocalDate.now().plusYears(1), CardStatus.BLOCKED, new BigDecimal("1000.00")
        );

        when(cardService.updateCardStatus(eq(1L), eq(CardStatus.BLOCKED))).thenReturn(response);

        mockMvc.perform(post("/admin/cards/1/status")
                        .param("status", "BLOCKED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.cardHolderName").value("John Doe"));

        verify(cardService).updateCardStatus(1L, CardStatus.BLOCKED);
    }

    @Test
    void updateCardStatus_InvalidStatus_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/admin/cards/1/status")
                        .param("status", "INVALID_STATUS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCardStatus_MissingStatusParam_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/admin/cards/1/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCard_ValidRequest_ShouldDeleteCard() throws Exception {
        doNothing().when(cardService).deleteCard(1L);

        mockMvc.perform(delete("/admin/cards/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(1L);
    }

    @Test
    void deleteCard_InvalidCardId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/admin/cards/abc") // нечисловой ID
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCard_MissingRequestBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCard_WithInvalidContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        CardResponseDto request = new CardResponseDto(
                1L, "************7890", "John Doe",
                LocalDate.now().plusYears(1), CardStatus.ACTIVE, new BigDecimal("1000.00"));

        mockMvc.perform(post("/admin/cards")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }
}