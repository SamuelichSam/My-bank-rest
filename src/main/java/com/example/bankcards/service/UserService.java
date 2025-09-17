package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDto updateUser(Long userId, UserUpdateDto userDto);

    void deleteUser(Long userId);

    Page<UserDto> getAllUsers(String search, Pageable pageable);

    UserDto getUserById(Long userId);

    void blockUser(Long userId);

    void unblockUser(Long userId);


    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    UserDetails loadUserByUsername(String username);

}
