package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.dto.*;
import com.bootcamp.makemycake.entities.*;
import com.bootcamp.makemycake.exceptions.paiement.NotFoundException;
import com.bootcamp.makemycake.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandeService {
    private final CommandeRepository commandeRepository;
    private final PatisserieRepository patisserieRepository;
    private final ClientRepository clientRepository;
    private final PanierRepository panierRepository;
    private final CoucheRepository coucheRepository;
    private final PricingService pricingService;
    private final CommandeNotificationService commandeNotificationService;

    @Transactional
    public CommandeDto creerCommande(CommandeRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client client = clientRepository.findByUserEmail(username)
                .orElseThrow(() -> new NotFoundException("Client non trouvé"));

        Patisserie patisserie = patisserieRepository.findById(request.getPatisserieId())
                .orElseThrow(() -> new NotFoundException("Pâtisserie non trouvée"));

        Panier panier = panierRepository.findByClientId(client.getId())
                .orElseGet(() -> {
                    Panier newPanier = new Panier();
                    newPanier.setClient(client);
                    return panierRepository.save(newPanier);
                });

        Commande commande = new Commande();
        commande.setClient(client);
        commande.setPatisserie(patisserie);
        commande.setPanier(panier);
        commande.setNombrePersonnes(request.getNombrePersonnes());
        commande.setGlacage(request.getGlacage());
        commande.setTelephoneClient(request.getTelephone());

        // Ajouter les couches
        request.getCouches().forEach(coucheReq -> {
            Couche couche = new Couche();
            couche.setSaveur(coucheReq.getSaveur());
            couche.setEpaisseur(coucheReq.getEpaisseur());
            couche.setPrix(pricingService.calculerPrixCouche(
                    coucheReq.getSaveur(),
                    coucheReq.getEpaisseur()
            ));
            couche.setCommande(commande);
            commande.getCouches().add(couche);
        });

        commande.calculerMontantTotal();
        Commande saved = commandeRepository.save(commande);
        CommandeDto dto = convertToDto(saved);

        // Ajoutez cette notification après la création
        commandeNotificationService.notifierNouvelleCommande(
                patisserie.getId(),
                dto
        );

        return dto;
    }

    public List<CommandeDto> getCommandesByCurrentClient() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client client = clientRepository.findByUserEmail(username)
                .orElseThrow(() -> new NotFoundException("Client non trouvé"));

        return commandeRepository.findByClientId(client.getId()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private CommandeDto convertToDto(Commande commande) {
        CommandeDto dto = new CommandeDto();
        dto.setId(commande.getId());
        dto.setDateCreation(commande.getDateCreation());
        dto.setMontantTotal(commande.getMontantTotal());
        dto.setStatut(commande.getStatut());
        dto.setNombrePersonnes(commande.getNombrePersonnes());
        dto.setGlacage(commande.getGlacage());
        dto.setTelephoneClient(commande.getTelephoneClient());
        dto.setPatisserieId(commande.getPatisserie().getId());
        dto.setPatisserieNom(commande.getPatisserie().getShopName());

        dto.setCouches(commande.getCouches().stream().map(couche -> {
            CommandeDto.CoucheDto coucheDto = new CommandeDto.CoucheDto();
            coucheDto.setSaveur(couche.getSaveur());
            coucheDto.setEpaisseur(couche.getEpaisseur());
            coucheDto.setPrix(couche.getPrix());
            return coucheDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    @Transactional
    public CommandeDto updateStatutCommande(Long commandeId, StatutCommande newStatut) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new NotFoundException("Commande non trouvée"));

        // Validate status transition logic if needed
        if (commande.getStatut() == StatutCommande.TERMINEE) {
            throw new IllegalStateException("Impossible de modifier une commande terminée");
        }

        commande.setStatut(newStatut);
        Commande updated = commandeRepository.save(commande);

        return convertToDto(updated);
    }
}