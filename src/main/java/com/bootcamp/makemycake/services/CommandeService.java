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
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommandeDto creerCommande(CommandeRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client client = clientRepository.findByUserEmail(username)
                .orElseThrow(() -> new NotFoundException("Client non trouvé"));

        Patisserie patisserie = patisserieRepository.findById(request.getPatisserieId())
                .orElseThrow(() -> new NotFoundException("Pâtisserie non trouvée"));

        Offre offre = offerRepository.findById(request.getOfferId())
                .orElseThrow(() -> new NotFoundException("Offre non trouvée"));

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

        // Add the layers (couches)
        request.getCouches().forEach(coucheReq -> {
            Couche couche = new Couche();
            couche.setSaveur(coucheReq.getSaveur());
            couche.setEpaisseur(coucheReq.getEpaisseur());
            couche.setPrix(pricingService.calculerPrixCouche(coucheReq.getSaveur(), coucheReq.getEpaisseur()).doubleValue());
            couche.setCommande(commande);
            commande.getCouches().add(couche);
        });

        // Calculate total price using the new pricing service
        double montantTotal = pricingService.calculerPrixTotal(
                request.getOfferId(),
                request.getNombrePersonnes(),
                request.getGlacage(),
                request.getCouches()
        ).doubleValue();
        commande.setMontantTotal(montantTotal);

        Commande savedCommande = commandeRepository.save(commande);
        commandeNotificationService.notifierNouvelleCommande(patisserie.getId(), convertToDto(savedCommande));

        return convertToDto(savedCommande);
    }

    public List<CommandeDto> getCommandesByCurrentClient() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Client client = clientRepository.findByUserEmail(username)
                .orElseThrow(() -> new NotFoundException("Client non trouvé"));

        return commandeRepository.findByClientId(client.getId()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<CommandeDto> getCommandesByCurrentPatisserie() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Supposons que vous ayez une méthode pour trouver la pâtisserie par l'email de l'utilisateur
        Patisserie patisserie = patisserieRepository.findByUserEmail(username)
                .orElseThrow(() -> new NotFoundException("Pâtisserie non trouvée"));

        return commandeRepository.findByPatisserieId(patisserie.getId()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<CommandeDto> getCommandesByPatissierId(Long patissierId) {
        // Verify patisserie exists
        if (!patisserieRepository.existsById(patissierId)) {
            throw new NotFoundException("Pâtisserie non trouvée");
        }

        // Get current user for authorization check
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));

        // Check if user is authorized to view these orders
        if (currentUser.getRole() == UserRole.PATISSIER) {
            Patisserie patisserie = patisserieRepository.findByUserEmail(username)
                    .orElseThrow(() -> new NotFoundException("Pâtisserie non trouvée"));
            if (!patisserie.getId().equals(patissierId)) {
                throw new SecurityException("Vous n'êtes pas autorisé à voir les commandes de cette pâtisserie");
            }
        }

        return commandeRepository.findByPatisserieId(patissierId).stream()
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

        // Ajout des infos client
        CommandeDto.ClientInfoDto clientInfo = new CommandeDto.ClientInfoDto();
        clientInfo.setId(commande.getClient().getId());
        clientInfo.setFullName(commande.getClient().getFullName());
        clientInfo.setEmail(commande.getClient().getUser().getEmail());
        clientInfo.setTelephone(commande.getTelephoneClient());
        dto.setClient(clientInfo);

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

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));

        // Check if the user is authorized to update this commande
        if (currentUser.getRole() == UserRole.CLIENT && 
            !commande.getClient().getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier cette commande");
        }

        if (currentUser.getRole() == UserRole.PATISSIER && 
            !commande.getPatisserie().getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier cette commande");
        }

        // Validate status transitions
        if (commande.getStatut() == StatutCommande.TERMINEE) {
            throw new IllegalStateException("Impossible de modifier une commande terminée");
        }

        // Only clients can cancel their own orders
        if (newStatut == StatutCommande.ANNULEE && currentUser.getRole() != UserRole.CLIENT) {
            throw new SecurityException("Seul le client peut annuler sa commande");
        }

        // Only patissiers can update to other statuses
        if (newStatut != StatutCommande.ANNULEE && currentUser.getRole() != UserRole.PATISSIER) {
            throw new SecurityException("Seul le pâtissier peut modifier le statut de la commande");
        }

        commande.setStatut(newStatut);
        Commande updated = commandeRepository.save(commande);

        return convertToDto(updated);
    }

    public Long getPatisserieIdByUsername(String username) {
        return patisserieRepository.findByUserEmail(username)
                .orElseThrow(() -> new NotFoundException("Pâtisserie non trouvée"))
                .getId();
    }
}