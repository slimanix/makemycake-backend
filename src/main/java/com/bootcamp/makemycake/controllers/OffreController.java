package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.OfferRequest;
import com.bootcamp.makemycake.entities.Offre;
import com.bootcamp.makemycake.exceptions.offre.AddOfferException;
import com.bootcamp.makemycake.exceptions.offre.DeleteOfferException;
import com.bootcamp.makemycake.exceptions.offre.OffreNotFoundException;
import com.bootcamp.makemycake.services.OfferService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offres")
public class OffreController {

    private final OfferService offerService;

    public OffreController(OfferService offerService) {
        this.offerService = offerService;
    }

    // 1. Création d'une offre
    @PostMapping
    public ResponseEntity<Offre> createOffer(@Valid @RequestBody OfferRequest request) {
        try {
            Offre newOffer = offerService.addOffer(request);
            return new ResponseEntity<>(newOffer, HttpStatus.CREATED);
        } catch (AddOfferException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // 2. Suppression d'une offre
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        try {
            offerService.deleteOffer(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (DeleteOfferException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 3. Récupération de toutes les offres (non paginée)
    @GetMapping
    public ResponseEntity<List<Offre>> getAllOffers() {
        return new ResponseEntity<>(offerService.getAllOffers(), HttpStatus.OK);
    }



    // 5. Récupération d'une offre par ID
    @GetMapping("/{id}")
    public ResponseEntity<Offre> getOfferById(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(offerService.getOfferById(id), HttpStatus.OK);
        } catch (OffreNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 6. Récupération des offres par pâtisserie (ID)
    @GetMapping("/patisserie/{patisserieId}")
    public ResponseEntity<List<Offre>> getOffersByPatisserie(@PathVariable Long patisserieId) {
        try {
            return new ResponseEntity<>(offerService.getOffersByPatisserieId(patisserieId), HttpStatus.OK);
        } catch (OffreNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}