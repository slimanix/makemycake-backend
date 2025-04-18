package com.bootcamp.makemycake.exceptions.paiement;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
