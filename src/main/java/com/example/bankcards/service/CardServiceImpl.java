package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.OperationNotAllowedException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements UserCardService, AdminCardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardEncryptionUtil encryptionUtil;

    @Override
    public Page<CardResponseDto> getUserCards(Long userId, String search, Pageable pageable) {
        Specification<Card> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId));
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("cardHolderName")), "%" + search.toLowerCase() + "%"));
        }
        return cardRepository.findAll(spec, pageable)
                .map(CardResponseDto::fromEntity);
    }

    @Override
    public CardResponseDto getCardById(Long cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));
        if (!user.getUserRole().equals(UserRole.ADMIN) && !card.getUser().getId().equals(user.getId())) {
            throw new OperationNotAllowedException("Access denied to this card");
        }
        return CardResponseDto.fromEntity(card);
    }

    @Override
    public void transferBetweenCards(TransferRequestDto request, User user) {
        Card fromcard = cardRepository.findById(request.fromCardId())
                .orElseThrow(() -> new CardNotFoundException("Source card not found"));
        Card tocard = cardRepository.findById(request.toCardId())
                .orElseThrow(() -> new CardNotFoundException("Destination card not found"));

        if (!fromcard.getUser().getId().equals(user.getId()) || !tocard.getUser().getId().equals(user.getId())) {
            throw new OperationNotAllowedException("You can only transfer between your own cards");
        }
        if (fromcard.isExpired() || fromcard.getStatus() != CardStatus.ACTIVE) {
            throw new OperationNotAllowedException("Source card expired or is not active");
        }
        if (tocard.isExpired() || tocard.getStatus() != CardStatus.ACTIVE) {
            throw new OperationNotAllowedException("Destination card expired or is not active");
        }
        if (fromcard.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds on source card");
        }

        fromcard.setBalance(fromcard.getBalance().subtract(request.amount()));
        tocard.setBalance(tocard.getBalance().add(request.amount()));
        cardRepository.save(fromcard);
        cardRepository.save(tocard);
    }

    @Override
    public BigDecimal getCardBalance(Long cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));
        if (!user.getUserRole().equals(UserRole.ADMIN) && !card.getUser().getId().equals(user.getId())) {
            throw new OperationNotAllowedException("Access denied to this card");
        }
        return card.getBalance();
    }

    @Override
    @Transactional
    public void requestBlockCard(Long cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new OperationNotAllowedException("You can only request block for your own cards");
        }
        if (card.isExpired()) {
            throw new OperationNotAllowedException("Cannot block expired card");
        }
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new OperationNotAllowedException("Card is already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Override
    public Page<CardResponseDto> getAllCards(String search, CardStatus status, Pageable pageable) {
        Specification<Card> spec = Specification.where(null);
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("cardHolderName")), "%" + search.toLowerCase() + "%"));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }
        return cardRepository.findAll(spec, pageable)
                .map(CardResponseDto::fromEntity);
    }


    @Override
    @Transactional
    public CardResponseDto createCard(CreateCardRequestDto request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.userId()));

        Card card = new Card();
        card.setCardNumber(encryptionUtil.encrypt(request.cardNumber()));
        card.setCardHolderName(request.cardHolderName());
        card.setExpirationDate(request.expirationDate());
        card.setBalance(request.initialBalance());
        card.setUser(user);
        card.updateStatus();

        Card savedCard = cardRepository.save(card);
        return CardResponseDto.fromEntity(savedCard);
    }

    @Override
    @Transactional
    public CardResponseDto updateCardStatus(Long cardId, CardStatus status) {
            Card card = cardRepository.findById(cardId)
                    .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

            if (card.isExpired() && status.equals(CardStatus.ACTIVE)) {
                throw new OperationNotAllowedException("Cannot activate expired card");
            }
            card.setStatus(status);
            Card updatedCard = cardRepository.save(card);
        return CardResponseDto.fromEntity(updatedCard);
    }

    @Override
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        if (card.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new OperationNotAllowedException("Cannot delete card with positive balance");
        }
        cardRepository.delete(card);
    }
}
