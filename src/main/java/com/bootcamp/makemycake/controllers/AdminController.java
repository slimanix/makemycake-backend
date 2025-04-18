package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.ApiResponse;
import com.bootcamp.makemycake.dto.PatisserieResponse;
import com.bootcamp.makemycake.entities.Patisserie;
import com.bootcamp.makemycake.entities.User;
import com.bootcamp.makemycake.exceptions.email.SendingEmailException;
import com.bootcamp.makemycake.exceptions.paiement.NotFoundException;
import com.bootcamp.makemycake.repositories.PatisserieRepository;
import com.bootcamp.makemycake.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private PatisserieRepository patisserieRepository;
    @Autowired
    private EmailService emailService;

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
    @Transactional
    public ResponseEntity<ApiResponse<PatisserieResponse>> validatePatisserie(
            @PathVariable Long id) {

        Patisserie patisserie = patisserieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patisserie non trouvée"));

        patisserie.setValid(true);
        Patisserie updated = patisserieRepository.save(patisserie);

        try {
            // Prepare email variables
            Map<String, String> emailVariables = Map.of(
                    "shopName", updated.getShopName()
            );

            // Load and populate email template
            String emailContent = emailService.loadEmailTemplate(
                    "templates/emails/patisserie-validation.html",
                    emailVariables
            );

            // Send email
            emailService.sendEmail(
                    updated.getUser().getEmail(),
                    "Votre patisserie a été validée !",
                    emailContent
            );

        } catch (Exception e) {
            // Handle both template loading and email sending exceptions
            System.err.println("Error sending validation email: " + e.getMessage());
            // You might want to log this properly in production
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        convertToResponse(updated),
                        "Patisserie validée avec succès",
                        HttpStatus.OK.value()
                )
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