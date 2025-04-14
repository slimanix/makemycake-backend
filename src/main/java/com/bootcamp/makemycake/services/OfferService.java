package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.dto.OfferRequest;
import com.bootcamp.makemycake.entities.Offre;
import com.bootcamp.makemycake.entities.Patisserie;
import com.bootcamp.makemycake.exceptions.offre.AddOfferException;
import com.bootcamp.makemycake.exceptions.offre.DeleteOfferException;
import com.bootcamp.makemycake.exceptions.offre.OffreNotFoundException;
import com.bootcamp.makemycake.repositories.OfferRepository;
import com.bootcamp.makemycake.repositories.PatisserieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@Transactional
public class OfferService {

    private final OfferRepository offerRepository;
    private final PatisserieRepository patisserieRepository;

    public OfferService(OfferRepository offerRepository,
                        PatisserieRepository patisserieRepository) {
        this.offerRepository = offerRepository;
        this.patisserieRepository = patisserieRepository;
    }

    /**
     * Crée et enregistre une nouvelle offre
     * @param request DTO contenant les informations de l'offre
     * @return L'offre créée
     * @throws AddOfferException Si la création échoue
     */
    public Offre addOffer(OfferRequest request) {
        try {
            Patisserie patisserie = patisserieRepository.findById(request.getPatisserieId())
                    .orElseThrow(() -> new AddOfferException("Pâtisserie non trouvée avec l'ID: " + request.getPatisserieId()));

            Offre offre = new Offre();
            offre.setTypeEvenement(request.getTypeEvenement());
            offre.setKilos(request.getKilos());
            offre.setPrix(request.getPrix());
            offre.setPhoto(request.getPhoto().getOriginalFilename()); // Adaptation nécessaire pour le stockage réel
            offre.setPatisserie(patisserie);
            offre.setValide(false); // Par défaut non validée par l'admin

            return offerRepository.save(offre);
        } catch (Exception e) {
            throw new AddOfferException("Échec de la création de l'offre: " + e.getMessage(), e);
        }
    }

    /**
     * Supprime une offre existante
     * @param offerId ID de l'offre à supprimer
     * @throws DeleteOfferException Si la suppression échoue
     */
    public void deleteOffer(Long offerId) {
        try {
            if (!offerRepository.existsById(offerId)) {
                throw new DeleteOfferException("Offre introuvable");
            }
            offerRepository.deleteById(offerId);
        } catch (Exception e) {
            throw new DeleteOfferException("Échec de la suppression de l'offre: " + e.getMessage(), e);
        }
    }

    /**
     * Récupère toutes les offres
     * @return Liste de toutes les offres
     */
    public List<Offre> getAllOffers() {
        return offerRepository.findAll();
    }

    /**
     * Récupère une offre par son ID
     * @param id ID de l'offre
     * @return L'offre correspondante
     * @throws DeleteOfferException Si l'offre n'est pas trouvée
     */
    public Offre getOfferById(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new DeleteOfferException("Offre non trouvée avec l'ID: " + id));
    }

    // 1. Récupération basique
    public List<Offre> getOffersByPatisserieId(Long patisserieId) {
        if (!patisserieRepository.existsById(patisserieId)) {
            throw new OffreNotFoundException("Pâtisserie introuvable");
        }
        return offerRepository.findByPatisserie_Id(patisserieId);  // Utilisez maintenant findByPatisserie_Id
    }

    // 2. Version paginée (inchangée)
    public Page<Offre> getOffersByPatisserieId(Long patisserieId, int page, int size) {
        return offerRepository.findByPatisserieId(
                patisserieId,
                PageRequest.of(page, size, Sort.by("prix").ascending())
        );
    }



}