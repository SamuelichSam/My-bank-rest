package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = "cards")
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> findBySearchWithCards(@Param("search") String search, Pageable pageable);

    @EntityGraph(attributePaths = "cards")
    @Query("SELECT u FROM User u")
    Page<User> findAllUsersWithCards(Pageable pageable);

    @EntityGraph(attributePaths = "cards")
    Optional<User> findWithCardsById(Long userId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
