package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.*;
import com.bootcamp.makemycake.entities.StatutCommande;
import com.bootcamp.makemycake.services.CommandeService;
import com.bootcamp.makemycake.services.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
public class CommandeController {
    private final CommandeService commandeService;
    private final PaiementService paiementService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CommandeDto> creerCommande(
            @RequestBody CommandeRequest request) {
        return ResponseEntity.ok(commandeService.creerCommande(request));
    }

    @PostMapping("/{commandeId}/paiement")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaiementDto> effectuerPaiement(
            @PathVariable Long commandeId,
            @RequestBody PaiementRequest request) {
        return ResponseEntity.ok(paiementService.processPaiement(commandeId, request));
    }

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<CommandeDto>> getCommandesClient() {
        return ResponseEntity.ok(commandeService.getCommandesByCurrentClient());
    }

    @PutMapping("/{commandeId}/statut")
    @PreAuthorize("hasAnyRole('PATISSIER', 'CLIENT')")
    public ResponseEntity<CommandeDto> updateStatut(
            @PathVariable Long commandeId,
            @RequestParam StatutCommande statut) {

        return ResponseEntity.ok(
                commandeService.updateStatutCommande(commandeId, statut)
        );
    }

    @GetMapping("/patisserie")
    @PreAuthorize("hasRole('PATISSIER')")
    public ResponseEntity<List<CommandeDto>> getCommandesPatisserie() {
        return ResponseEntity.ok(commandeService.getCommandesByCurrentPatisserie());
    }

}