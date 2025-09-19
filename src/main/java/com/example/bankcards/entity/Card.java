package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bank_cards")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @Column(name = "card_number")
    private String cardNumber;
    private String cardHolderName;
    private LocalDate expirationDate;
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    private CardStatus status;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public boolean isExpired() {
        return expirationDate.isBefore(LocalDate.now());
    }

    public void updateStatus() {
        if (isExpired()) {
            this.status = CardStatus.EXPIRED;
        }
    }
}
