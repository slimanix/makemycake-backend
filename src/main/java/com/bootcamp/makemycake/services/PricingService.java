package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.entities.Offre;
import com.bootcamp.makemycake.repositories.OfferRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PricingService {
    private final OfferRepository offerRepository;

    public PricingService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    private static final Map<String, Double> FLAVOR_MULTIPLIERS = Map.of(
            "chocolat", 1.2,
            "pistache", 1.3,
            "caramel", 1.2,
            "redvelvet", 1.4
    );

    private static final double BASE_LAYER_PRICE = 50.0;

    public double calculerPrixCouche(String saveur, int epaisseur) {
        double flavorMultiplier = FLAVOR_MULTIPLIERS.getOrDefault(saveur.toLowerCase(), 1.0);
        return BASE_LAYER_PRICE * (epaisseur / 3.0) * flavorMultiplier;
    }

    public double calculerPrixGlacage(String glacage) {
        return switch(glacage.toLowerCase()) {
            case "fondant" -> 200.0;
            case "ganache" -> 150.0;
            case "creme" -> 100.0;
            default -> 0.0;
        };
    }

    public double calculerPrixBase(Long offerId, int nombrePersonnes) {
        Offre offre = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        return offre.getPrix() * (nombrePersonnes / 2.0);
    }

    public double calculerPrixTotal(Long offerId, int nombrePersonnes, String glacage, int nombreCouches) {
        double prixBase = calculerPrixBase(offerId, nombrePersonnes);
        double prixGlacage = calculerPrixGlacage(glacage);
        double prixCouches = 0.0;

        // Only calculate price for layers beyond the third
        if (nombreCouches > 3) {
            int extraLayers = nombreCouches - 3;
            // Assuming all extra layers have the same flavor and thickness for simplicity
            // In a real scenario, you would need to pass the actual layer details
            prixCouches = extraLayers * calculerPrixCouche("chocolat", 3); // Default values
        }

        return prixBase + prixCouches + prixGlacage;
    }
}