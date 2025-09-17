package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.DuplicateEmailException;
import com.example.bankcards.exception.DuplicateUsernameException;
import com.example.bankcards.exception.UserHasCardsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserUpdateDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (!user.getUsername().equals(updateDto.username()) &&
                userRepository.existsByUsername(updateDto.username())) {
            throw new DuplicateUsernameException("Username already exists: " + updateDto.username());
        }
        if (!user.getEmail().equals(updateDto.email()) &&
                userRepository.existsByEmail(updateDto.email())) {
            throw new DuplicateEmailException("Email already exists: " + updateDto.email());
        }

        user.setUsername(updateDto.username());
        user.setEmail(updateDto.email());
        user.setEnabled(updateDto.enabled());
        user.setUserRole(updateDto.userRole());
        User updatedUser = userRepository.save(user);
        return UserDto.fromEntity(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (cardRepository.existsByUserId(userId)) {
            throw new UserHasCardsException("Cannot delete user with id " + userId + " because they have associated cards");
        }

        userRepository.delete(user);
    }

    @Override
    public Page<UserDto> getAllUsers(String search, Pageable pageable) {
        Page<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.findBySearchWithCards(search, pageable);
        } else {
            users = userRepository.findAllUsersWithCards(pageable);
        }
        return users.map(UserDto::fromEntity);
    }

    @Override
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findWithCardsById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return UserDto.fromEntity(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
