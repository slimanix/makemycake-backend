package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.ApiResponse;
import com.bootcamp.makemycake.dto.PatisserieResponse;
import com.bootcamp.makemycake.entities.Patisserie;
import com.bootcamp.makemycake.repositories.PatisserieRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private PatisserieRepository patisserieRepository;

    // 1. Get all patisseries
    @GetMapping("/patisseries")
    public ResponseEntity<ApiResponse<List<PatisserieResponse>>> getAllPatisseries() {
        List<PatisserieResponse> responses = patisserieRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>(responses, "Liste complète des pâtisseries", HttpStatus.OK.value())
        );
    }

    // 2. Validate a patisserie
    @PutMapping("/patisseries/{id}/validate")
    public ResponseEntity<ApiResponse<PatisserieResponse>> validatePatisserie(
            @PathVariable Long id,
            @RequestParam(required = false) String siretNumber) {

        Patisserie patisserie = patisserieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pâtisserie non trouvée (ID: " + id));

        patisserie.setValid(true);
        if (siretNumber != null && !siretNumber.isBlank()) {
            patisserie.setSiretNumber(siretNumber.trim());
        }

        Patisserie updated = patisserieRepository.save(patisserie);

        return ResponseEntity.ok(
                new ApiResponse<>(convertToResponse(updated),
                        "Pâtisserie validée avec succès",
                        HttpStatus.OK.value())
        );
    }

    // 3. Get validated patisseries
    @GetMapping("/patisseries/valides")
    public ResponseEntity<ApiResponse<List<PatisserieResponse>>> getValidatedPatisseries() {
        List<PatisserieResponse> responses = patisserieRepository.findAllVerifiedPatisseries()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>(responses, "Pâtisseries validées", HttpStatus.OK.value())
        );
    }

    // 4. Get non-validated patisseries (NEW)
    @GetMapping("/patisseries/non-valides")
    public ResponseEntity<ApiResponse<List<PatisserieResponse>>> getNonValidatedPatisseries() {
        List<PatisserieResponse> responses = patisserieRepository.findAllNonValidatedPatisseries()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>(responses, "Pâtisseries non validées", HttpStatus.OK.value())
        );
    }

    private PatisserieResponse convertToResponse(Patisserie patisserie) {
        PatisserieResponse response = new PatisserieResponse();
        response.setId(patisserie.getId());
        response.setShopName(patisserie.getShopName());
        response.setPhoneNumber(patisserie.getPhoneNumber());
        response.setLocation(patisserie.getLocation());
        response.setProfilePicture(patisserie.getProfilePicture());
        response.setSiretNumber(patisserie.getSiretNumber());
        response.setValid(patisserie.isValid()); // Make sure this exists in your DTO

        if (patisserie.getUser() != null) {
            response.setUserEmail(patisserie.getUser().getEmail());
        }

        if (patisserie.getOffres() != null) {
            response.setNombreOffres(patisserie.getOffres().size());
        }

        return response;
    }
}