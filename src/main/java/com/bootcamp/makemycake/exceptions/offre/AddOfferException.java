package com.bootcamp.makemycake.exceptions.offre;

public class AddOfferException extends RuntimeException {

    // Constructeur avec message seul
    public AddOfferException(String message) {
        super(message);
    }

    // Constructeur avec message + cause (exception originale)
    public AddOfferException(String message, Throwable cause) {
        super(message, cause);
    }
}