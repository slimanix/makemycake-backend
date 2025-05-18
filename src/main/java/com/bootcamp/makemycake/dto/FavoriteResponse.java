package com.bootcamp.makemycake.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
    private Long id;
    private Long clientId;
    private Long offerId;
    private LocalDateTime createdAt;
    private OffreResponse offerDetails;
} 