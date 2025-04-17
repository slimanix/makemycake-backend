package com.bootcamp.makemycake.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PatisserieResponse {
    private Long id;
    private String shopName;
    private String phoneNumber;
    private String location;
    private String profilePicture;
    private String siretNumber;

    // Information minimale sur l'utilisateur
    private String userEmail;

    // Nombre d'offres au lieu de la liste complète
    private int nombreOffres;

    private boolean isValid; // Add this new field

}