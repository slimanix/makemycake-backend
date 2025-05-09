package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.dto.CommandeRequest.CoucheRequest;
import com.bootcamp.makemycake.entities.Offre;
import com.bootcamp.makemycake.repositories.OfferRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class PricingService {
    private final OfferRepository offerRepository;

    public PricingService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    // Price constants
    private static final BigDecimal BASE_LAYER_PRICE = new BigDecimal("50.0");
    private static final BigDecimal FONDANT_PRICE = new BigDecimal("200.0");
    private static final BigDecimal GANACHE_PRICE = new BigDecimal("150.0");
    private static final BigDecimal CREME_PRICE = new BigDecimal("100.0");

    private static final Map<String, BigDecimal> FLAVOR_MULTIPLIERS = Map.of(
            "chocolat", new BigDecimal("1.2"),
            "pistache", new BigDecimal("1.3"),
            "caramel", new BigDecimal("1.2"),
            "redvelvet", new BigDecimal("1.4")
    );

    public BigDecimal calculerPrixCouche(String saveur, int epaisseur) {
        BigDecimal flavorMultiplier = FLAVOR_MULTIPLIERS.getOrDefault(saveur.toLowerCase(), BigDecimal.ONE);
        BigDecimal thicknessMultiplier = new BigDecimal(epaisseur).divide(new BigDecimal("3.0"), 2, RoundingMode.HALF_UP);
        return BASE_LAYER_PRICE.multiply(thicknessMultiplier).multiply(flavorMultiplier);
    }

    public BigDecimal calculerPrixGlacage(String glacage) {
        if (glacage == null) return BigDecimal.ZERO;
        
        return switch(glacage.toLowerCase()) {
            case "fondant" -> FONDANT_PRICE;
            case "ganache" -> GANACHE_PRICE;
            case "creme" -> CREME_PRICE;
            default -> BigDecimal.ZERO;
        };
    }

    public BigDecimal calculerPrixBase(Long offerId, int nombrePersonnes) {
        Offre offre = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        return new BigDecimal(offre.getPrix())
                .multiply(new BigDecimal(nombrePersonnes))
                .divide(new BigDecimal("2.0"), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculerPrixTotal(Long offerId, int nombrePersonnes, String glacage, List<CoucheRequest> couches) {
        BigDecimal prixBase = calculerPrixBase(offerId, nombrePersonnes);
        BigDecimal prixGlacage = calculerPrixGlacage(glacage);
        BigDecimal prixCouches = BigDecimal.ZERO;

        // Only calculate price for layers beyond the third
        if (couches.size() > 3) {
            prixCouches = couches.subList(3, couches.size()).stream()
                .map(couche -> calculerPrixCouche(couche.getSaveur(), couche.getEpaisseur()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return prixBase.add(prixCouches).add(prixGlacage);
    }
}