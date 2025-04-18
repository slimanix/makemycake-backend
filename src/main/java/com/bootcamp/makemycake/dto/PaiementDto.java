package com.bootcamp.makemycake.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class PaiementDto {
    private Long id;
    private Double montant;
    private String methode;
    private String statut;
    private LocalDateTime datePaiement;
}