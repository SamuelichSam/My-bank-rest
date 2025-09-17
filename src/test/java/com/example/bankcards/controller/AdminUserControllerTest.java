package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.service.UserService;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setFallbackPageable(PageRequest.of(0, 20));

        mockMvc = MockMvcBuilders.standaloneSetup(adminUserController)
                .setCustomArgumentResolvers(pageableResolver)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void updateUser_ValidRequest_ShouldUpdateUser() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto("newuser", "new@email.com", true, UserRole.USER);
        UserDto userDto = new UserDto(1L, "newuser", "new@email.com", true, UserRole.USER, null);

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("newuser"));

        verify(userService).updateUser(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    void updateUser_InvalidUserId_ShouldReturnBadRequest() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto("newuser", "new@email.com", true, UserRole.USER);

        mockMvc.perform(post("/admin/users/invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_ValidRequest_ShouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void getUserById_ValidRequest_ShouldReturnUser() throws Exception {
        UserDto userDto = new UserDto(1L, "testuser", "test@email.com", true, UserRole.USER, null);
        when(userService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).getUserById(1L);
    }

    @Test
    void getAllUsers_WithoutSearch_ShouldReturnUsers() throws Exception {
        UserDto userDto = new UserDto(1L, "testuser", "test@email.com", true, UserRole.USER, null);
        Page<UserDto> page = new PageImpl<>(List.of(userDto), PageRequest.of(0, 20), 1);

        when(userService.getAllUsers(eq(null), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).getAllUsers(eq(null), any(Pageable.class));
    }

    @Test
    void getAllUsers_WithSearch_ShouldReturnFilteredUsers() throws Exception {
        UserDto userDto = new UserDto(1L, "testuser", "test@email.com", true, UserRole.USER, null);

        Page<UserDto> page = new PageImpl<>(List.of(userDto), PageRequest.of(0, 20), 1);

        when(userService.getAllUsers(eq("test"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/admin/users")
                        .param("search", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).getAllUsers(eq("test"), any(Pageable.class));
    }

    @Test
    void blockUser_ValidRequest_ShouldBlockUser() throws Exception {
        doNothing().when(userService).blockUser(1L);

        mockMvc.perform(patch("/admin/users/1/block")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).blockUser(1L);
    }

    @Test
    void unblockUser_ValidRequest_ShouldUnblockUser() throws Exception {
        doNothing().when(userService).unblockUser(1L);

        mockMvc.perform(patch("/admin/users/1/unblock")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).unblockUser(1L);
    }

    @Test
    void updateUser_MissingRequestBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_InvalidContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto("newuser", "new@email.com", true, UserRole.USER);

        mockMvc.perform(post("/admin/users/1")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void deleteUser_InvalidUserId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/admin/users/invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_InvalidUserId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/admin/users/invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blockUser_InvalidUserId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/users/invalid/block")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unblockUser_InvalidUserId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/users/invalid/unblock")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}