package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.OfferRequest;
import com.bootcamp.makemycake.dto.OffreResponse;
import com.bootcamp.makemycake.dto.OffreDetailsResponse;
import com.bootcamp.makemycake.services.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offres")
@RequiredArgsConstructor
public class OffreController {

    private final OfferService offerService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OffreResponse> createOffer(
            @Valid @ModelAttribute OfferRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(offerService.createOffer(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<OffreResponse>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OffreResponse> getOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }

    @GetMapping("/patisserie/{patisserieId}")
    public ResponseEntity<List<OffreResponse>> getOffersByPatisserie(
            @PathVariable Long patisserieId) {
        return ResponseEntity.ok(offerService.getOffersByPatisserie(patisserieId));
    }

    @GetMapping("/patisserie/{patisserieId}/paginated")
    public ResponseEntity<Page<OffreResponse>> getOffersByPatisseriePaginated(
            @PathVariable Long patisserieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                offerService.getOffersByPatisseriePaginated(patisserieId, page, size));
    }

    @PutMapping("/{id}/validate")
    @PreAuthorize("hasRole('ADMIN')") // Sécurisez l'accès
    public ResponseEntity<OffreResponse> validateOffer(
            @PathVariable Long id,
            @RequestParam boolean isValid) {

        return ResponseEntity.ok(
                offerService.validateOffer(id, isValid)
        );
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<OffreDetailsResponse> getOfferDetailsById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferDetailsById(id));
    }

    @GetMapping("/ville/{ville}")
    public ResponseEntity<List<OffreResponse>> getOffersByVille(@PathVariable String ville) {
        return ResponseEntity.ok(offerService.getOffersByVille(ville));
    }

    @GetMapping("/ville/{ville}/paginated")
    public ResponseEntity<Page<OffreResponse>> getOffersByVillePaginated(
            @PathVariable String ville,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(offerService.getOffersByVillePaginated(ville, page, size));
    }

    @GetMapping("/ville/{ville}/patisserie/{patisserieId}")
    public ResponseEntity<List<OffreResponse>> getOffersByVilleAndPatisserie(
            @PathVariable String ville,
            @PathVariable Long patisserieId) {
        return ResponseEntity.ok(offerService.getOffersByVilleAndPatisserie(ville, patisserieId));
    }
}