package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.ApiResponse;
import com.bootcamp.makemycake.dto.PatisserieResponse;
import com.bootcamp.makemycake.dto.PatisserieUpdateRequest;
import com.bootcamp.makemycake.entities.Patisserie;
import com.bootcamp.makemycake.repositories.PatisserieRepository;
import com.bootcamp.makemycake.services.AuthService;
import com.bootcamp.makemycake.services.CloudinaryService;
import com.bootcamp.makemycake.services.SecurityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patisseries")
@CrossOrigin(origins = "http://localhost:4200")
public class PatisserieController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PatisserieRepository patisserieRepository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private CloudinaryService cloudinaryService;

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

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PATISSIER') and @securityService.isPatisserieOwner(#id)")
    public ResponseEntity<ApiResponse<PatisserieResponse>> updatePatisserie(
            @PathVariable Long id,
            @Valid @ModelAttribute PatisserieUpdateRequest request) {
        
        Patisserie patisserie = patisserieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pâtisserie non trouvée"));

        // Update basic information
        patisserie.setShopName(request.getShopName());
        patisserie.setPhoneNumber(request.getPhoneNumber());
        patisserie.setLocation(request.getLocation());

        // Handle banner upload if provided
        if (request.getBanner() != null && !request.getBanner().isEmpty()) {
            String bannerUrl = cloudinaryService.uploadFile(request.getBanner());
            patisserie.setProfilePicture(bannerUrl);
        }

        Patisserie updatedPatisserie = patisserieRepository.save(patisserie);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        convertToResponse(updatedPatisserie),
                        "Pâtisserie mise à jour avec succès",
                        HttpStatus.OK.value()
                )
        );
    }
}