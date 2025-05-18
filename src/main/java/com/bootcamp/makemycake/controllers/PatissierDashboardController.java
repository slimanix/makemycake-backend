package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.*;
import com.bootcamp.makemycake.entities.StatutCommande;
import com.bootcamp.makemycake.services.CommandeService;
import com.bootcamp.makemycake.services.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patissier/dashboard")
@RequiredArgsConstructor
@Tag(name = "Patissier Dashboard", description = "APIs for patissier dashboard statistics")
public class PatissierDashboardController {

    private final CommandeService commandeService;
    private final OfferService offerService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('PATISSIER')")
    @Operation(summary = "Get order statistics for the current patissier")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        List<CommandeDto> allOrders = commandeService.getCommandesByCurrentPatisserie();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCommandes", allOrders.size());
        stats.put("commandesTerminees", countOrdersByStatus(allOrders, StatutCommande.TERMINEE));
        stats.put("commandesEnAttente", countOrdersByStatus(allOrders, StatutCommande.EN_ATTENTE));
        stats.put("commandesEnPreparation", countOrdersByStatus(allOrders, StatutCommande.PREPARATION));
        stats.put("commandesEnLivraison", countOrdersByStatus(allOrders, StatutCommande.LIVRAISON));
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent-orders")
    @PreAuthorize("hasRole('PATISSIER')")
    @Operation(summary = "Get recent orders for the current patissier")
    public ResponseEntity<List<CommandeDto>> getRecentOrders(
            @RequestParam(defaultValue = "5") int limit) {
        List<CommandeDto> allOrders = commandeService.getCommandesByCurrentPatisserie();
        
        // Sort by dateCreation in descending order and limit the results
        List<CommandeDto> recentOrders = allOrders.stream()
                .sorted((o1, o2) -> o2.getDateCreation().compareTo(o1.getDateCreation()))
                .limit(limit)
                .toList();
        
        return ResponseEntity.ok(recentOrders);
    }

    @GetMapping("/recent-offers")
    @PreAuthorize("hasRole('PATISSIER')")
    @Operation(summary = "Get recent offers for the current patissier")
    public ResponseEntity<List<OffreResponse>> getRecentOffers(
            @RequestParam(defaultValue = "5") int limit) {
        // Get the current patissier's ID from the security context
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        
        // Get all offers and sort by creation date
        List<OffreResponse> allOffers = offerService.getOffersByPatisserie(
                commandeService.getPatisserieIdByUsername(username));
        
        // Sort and limit the results
        List<OffreResponse> recentOffers = allOffers.stream()
                .limit(limit)
                .toList();
        
        return ResponseEntity.ok(recentOffers);
    }

    private long countOrdersByStatus(List<CommandeDto> orders, StatutCommande status) {
        return orders.stream()
                .filter(order -> order.getStatut() == status)
                .count();
    }
} 