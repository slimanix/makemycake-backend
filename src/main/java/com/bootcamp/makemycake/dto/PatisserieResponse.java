package com.bootcamp.makemycake.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
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