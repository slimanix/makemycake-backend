package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.dto.OfferRequest;
import com.bootcamp.makemycake.dto.OffreResponse;
import com.bootcamp.makemycake.entities.Offre;
import com.bootcamp.makemycake.entities.Patisserie;
import com.bootcamp.makemycake.entities.User;
import com.bootcamp.makemycake.entities.UserRole;
import com.bootcamp.makemycake.exceptions.offre.AddOfferException;
import com.bootcamp.makemycake.exceptions.offre.DeleteOfferException;
import com.bootcamp.makemycake.exceptions.offre.OffreNotFoundException;
import com.bootcamp.makemycake.repositories.OfferRepository;
import com.bootcamp.makemycake.repositories.PatisserieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final PatisserieRepository patisserieRepository;

    public OffreResponse createOffer(OfferRequest request) {
        try {
            Patisserie patisserie = patisserieRepository.findById(request.getPatisserieId())
                    .orElseThrow(() -> new AddOfferException("Pâtisserie non trouvée"));

            Offre offre = Offre.builder()
                    .typeEvenement(request.getTypeEvenement())
                    .kilos(request.getKilos())
                    .prix(request.getPrix())
                    .photo(request.getPhoto().getOriginalFilename())
                    .patisserie(patisserie)
                    .valide(false)
                    .build();

            Offre savedOffre = offerRepository.save(offre);
            return convertToResponse(savedOffre);
        } catch (Exception e) {
            throw new AddOfferException("Échec création offre: " + e.getMessage(), e);
        }
    }

    public void deleteOffer(Long offerId) {
        if (!offerRepository.existsById(offerId)) {
            throw new DeleteOfferException("Offre introuvable");
        }
        offerRepository.deleteById(offerId);
    }

    public List<OffreResponse> getAllOffers() {
        return offerRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<OffreResponse> getValidOffers() {
        return offerRepository.findByValideTrue().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public OffreResponse getOfferById(Long id) {
        Offre offre = offerRepository.findById(id)
                .orElseThrow(() -> new OffreNotFoundException("Offre introuvable"));
        return convertToResponse(offre);
    }

    public List<OffreResponse> getOffersByPatisserie(Long patisserieId) {
        if (!patisserieRepository.existsById(patisserieId)) {
            throw new OffreNotFoundException("Pâtisserie introuvable");
        }
        return offerRepository.findByPatisserie_Id(patisserieId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Page<OffreResponse> getOffersByPatisseriePaginated(Long patisserieId, int page, int size) {
        return offerRepository.findByPatisserieId(patisserieId,
                        PageRequest.of(page, size, Sort.by("prix").ascending()))
                .map(this::convertToResponse);
    }

    public OffreResponse validateOffer(Long offerId, boolean isValid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Vérification du rôle ADMIN
        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new SecurityException("Accès refusé : rôle admin requis");
        }

        Offre offre = offerRepository.findById(offerId)
                .orElseThrow(() -> new OffreNotFoundException("Offre non trouvée"));

        offre.setValide(isValid);
        offre.setAdmin(isValid ? currentUser : null);

        Offre updatedOffre = offerRepository.save(offre);

        return convertToResponse(updatedOffre);
    }

    private OffreResponse convertToResponse(Offre offre) {
        return OffreResponse.builder()
                .id(offre.getId())
                .typeEvenement(offre.getTypeEvenement())
                .kilos(offre.getKilos())
                .prix(offre.getPrix())
                .photoUrl(offre.getPhoto())
                .valide(offre.getValide())
                .patisserieId(offre.getPatisserie().getId())
                .patisserieNom(offre.getPatisserie().getShopName())
                // Utilisation de l'email comme identifiant admin
                .validatedByAdminId(offre.getAdmin() != null ? offre.getAdmin().getId() : null)
                .validatedByAdminName(offre.getAdmin() != null ? offre.getAdmin().getEmail() : null)
                .build();
    }
}