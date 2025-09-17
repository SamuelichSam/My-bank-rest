package com.example.bankcards.exception;

public class UserHasCardsException extends RuntimeException {
    public UserHasCardsException(String message) {
        super(message);
    }
}
