package com.bootcamp.makemycake.services;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PricingService {
    private static final Map<String, Double> PRIX_SAVEUR = Map.of(
            "chocolat", 15.0,
            "vanille", 12.0,
            "fraise", 18.0
    );

    private static final double COEFF_EPAISSEUR = 1.5;

    public double calculerPrixCouche(String saveur, int epaisseur) {
        return PRIX_SAVEUR.getOrDefault(saveur.toLowerCase(), 10.0) * (epaisseur * COEFF_EPAISSEUR);
    }

    public double calculerPrixGlacage(String glacage, int nbPersonnes) {
        return nbPersonnes * 0.5 *
                switch(glacage.toLowerCase()) {
                    case "fondant" -> 20.0;
                    case "miroir" -> 25.0;
                    default -> 15.0;
                };
    }
}