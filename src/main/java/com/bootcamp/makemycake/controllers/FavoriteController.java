package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.FavoriteResponse;
import com.bootcamp.makemycake.dto.OffreResponse;
import com.bootcamp.makemycake.entities.Client;
import com.bootcamp.makemycake.entities.Favorite;
import com.bootcamp.makemycake.entities.Offre;
import com.bootcamp.makemycake.repositories.ClientRepository;
import com.bootcamp.makemycake.services.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final ClientRepository clientRepository;

    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<FavoriteResponse>> getClientFavorites() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        List<Favorite> favorites = favoriteService.getClientFavorites(client.getId());
        List<FavoriteResponse> response = favorites.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{offerId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<FavoriteResponse> addToFavorites(@PathVariable Long offerId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Favorite favorite = favoriteService.addToFavorites(client.getId(), offerId);
        FavoriteResponse response = convertToResponse(favorite);
        
        // Check if this is a new favorite or an existing one
        boolean isNewFavorite = favorite.getCreatedAt().equals(favorite.getCreatedAt());
        HttpStatus status = isNewFavorite ? HttpStatus.CREATED : HttpStatus.OK;
        
        return ResponseEntity.status(status).body(response);
    }

    @DeleteMapping("/{offerId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable Long offerId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        favoriteService.removeFromFavorites(client.getId(), offerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{offerId}/status")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Boolean> isFavorite(@PathVariable Long offerId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        boolean isFavorite = favoriteService.isFavorite(client.getId(), offerId);
        return ResponseEntity.ok(isFavorite);
    }

    private FavoriteResponse convertToResponse(Favorite favorite) {
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .clientId(favorite.getClient().getId())
                .offerId(favorite.getOffre().getId())
                .createdAt(favorite.getCreatedAt())
                .offerDetails(convertToOffreResponse(favorite.getOffre()))
                .build();
    }

    private OffreResponse convertToOffreResponse(Offre offre) {
        return OffreResponse.builder()
                .id(offre.getId())
                .typeEvenement(offre.getTypeEvenement())
                .kilos(offre.getKilos())
                .prix(offre.getPrix())
                .photoUrl(offre.getPhoto())
                .ville(offre.getVille())
                .valide(offre.getValide())
                .patisserieId(offre.getPatisserie().getId())
                .patisserieNom(offre.getPatisserie().getShopName())
                .build();
    }
} 