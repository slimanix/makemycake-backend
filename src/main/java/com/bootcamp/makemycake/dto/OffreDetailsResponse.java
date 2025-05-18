package com.bootcamp.makemycake.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class OffreDetailsResponse {
    private Long id;
    private String typeEvenement;
    private Double kilos;
    private Double prix;
    private String photoUrl;
    private String ville;
    private boolean valide;
    private Long validatedByAdminId;
    private String validatedByAdminName;
    private PatisserieResponse patisserie;
    private LocalDateTime createdAt;
} 