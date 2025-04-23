package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.ApiResponse;
import com.bootcamp.makemycake.dto.PatisserieResponse;
import com.bootcamp.makemycake.entities.Patisserie;
import com.bootcamp.makemycake.repositories.PatisserieRepository;
import com.bootcamp.makemycake.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patisseries")
public class PatisserieController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PatisserieRepository patisserieRepository;

    @PatchMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<String>> validatePatisserie(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        // Ici vous pourriez ajouter une vérification du rôle ADMIN
        // avant de permettre la validation

        ApiResponse<String> response = authService.validatePatisserie(id);
        return ResponseEntity.status(response.getStatus()).body(response);
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

    // 3. Get validated patisseries
    @GetMapping("/valides")
    public ResponseEntity<ApiResponse<List<PatisserieResponse>>> getValidatedPatisseries() {
        List<PatisserieResponse> responses = patisserieRepository.findAllVerifiedPatisseries()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>(responses, "Pâtisseries validées", HttpStatus.OK.value())
        );
    }
}