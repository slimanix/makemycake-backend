package com.bootcamp.makemycake.exceptions.offre;

public class DeleteOfferException extends RuntimeException {

    // Constructeur avec message uniquement
    public DeleteOfferException(String message) {
        super(message);
    }

    // Constructeur avec message ET cause (exception originale)
    public DeleteOfferException(String message, Throwable cause) {
        super(message, cause);
    }
}