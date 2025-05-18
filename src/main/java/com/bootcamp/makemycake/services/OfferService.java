package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.dto.OfferRequest;
import com.bootcamp.makemycake.dto.OffreResponse;
import com.bootcamp.makemycake.dto.OffreDetailsResponse;
import com.bootcamp.makemycake.dto.PatisserieResponse;
import com.bootcamp.makemycake.entities.Offre;
import com.bootcamp.makemycake.entities.Patisserie;
import com.bootcamp.makemycake.entities.User;
import com.bootcamp.makemycake.entities.UserRole;
import com.bootcamp.makemycake.exceptions.offre.AddOfferException;
import com.bootcamp.makemycake.exceptions.offre.DeleteOfferException;
import com.bootcamp.makemycake.exceptions.offre.OffreNotFoundException;
import com.bootcamp.makemycake.models.City;
import com.bootcamp.makemycake.repositories.OfferRepository;
import com.bootcamp.makemycake.repositories.PatisserieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final PatisserieRepository patisserieRepository;
    private final CloudinaryService cloudinaryService;
    private final CityService cityService;

    public OffreResponse createOffer(OfferRequest request) {
        try {
            log.info("Incoming offer creation payload: {}", request);

            // Validate ville field
            if (request.getVille() == null || request.getVille().isEmpty()) {
                log.warn("Offer creation failed: missing ville");
                throw new AddOfferException("Invalid or missing city (ville) selection.");
            }
            City city = cityService.getCityByName(request.getVille());
            if (city == null) {
                log.warn("Offer creation failed: invalid city name {}", request.getVille());
                throw new AddOfferException("The selected city does not exist. Please select a valid city from the list.");
            }

            // Validate patisserie exists
            Patisserie patisserie = patisserieRepository.findById(request.getPatisserieId())
                    .orElseThrow(() -> new AddOfferException("Pâtisserie non trouvée"));

            // Validate and upload photo
            if (request.getPhoto() == null || request.getPhoto().isEmpty()) {
                throw new AddOfferException("Photo requise");
            }

            String photoUrl = cloudinaryService.uploadFile(request.getPhoto());

            // Build and save offer
            Offre offre = Offre.builder()
                    .typeEvenement(request.getTypeEvenement())
                    .kilos(request.getKilos())
                    .prix(request.getPrix())
                    .ville(request.getVille())
                    .photo(photoUrl)
                    .patisserie(patisserie)
                    .valide(false)
                    .build();

            Offre savedOffre = offerRepository.save(offre);
            log.info("Offer created successfully with ID: {}", savedOffre.getId());

            return convertToResponse(savedOffre);

        } catch (AddOfferException e) {
            log.error("Error creating offer: {} | Payload: {}", e.getMessage(), request);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating offer: {} | Payload: {}", e.getMessage(), request);
            throw new AddOfferException("Erreur inattendue lors de la création de l'offre", e);
        }
    }

    public void deleteOffer(Long offerId) {
        try {
            if (!offerRepository.existsById(offerId)) {
                throw new DeleteOfferException("Offre introuvable");
            }
            offerRepository.deleteById(offerId);
            log.info("Offer deleted successfully with ID: {}", offerId);
        } catch (DeleteOfferException e) {
            log.error("Error deleting offer: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting offer: {}", e.getMessage());
            throw new DeleteOfferException("Erreur inattendue lors de la suppression de l'offre", e);
        }
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
        try {
            Offre offre = offerRepository.findById(id)
                    .orElseThrow(() -> new OffreNotFoundException("Offre introuvable"));
            return convertToResponse(offre);
        } catch (OffreNotFoundException e) {
            log.error("Offer not found: {}", id);
            throw e;
        }
    }

    public List<OffreResponse> getOffersByPatisserie(Long patisserieId) {
        try {
            if (!patisserieRepository.existsById(patisserieId)) {
                throw new OffreNotFoundException("Pâtisserie introuvable");
            }
            return offerRepository.findByPatisserie_Id(patisserieId).stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (OffreNotFoundException e) {
            log.error("Patisserie not found: {}", patisserieId);
            throw e;
        }
    }

    public Page<OffreResponse> getOffersByPatisseriePaginated(Long patisserieId, int page, int size) {
        return offerRepository.findByPatisserieId(patisserieId,
                        PageRequest.of(page, size, Sort.by("prix").ascending()))
                .map(this::convertToResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public OffreResponse validateOffer(Long offerId, boolean isValid) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();

            if (!currentUser.getRole().equals(UserRole.ADMIN)) {
                throw new SecurityException("Accès refusé : rôle admin requis");
            }

            Offre offre = offerRepository.findById(offerId)
                    .orElseThrow(() -> new OffreNotFoundException("Offre non trouvée"));

            offre.setValide(isValid);
            offre.setAdmin(isValid ? currentUser : null);

            Offre updatedOffre = offerRepository.save(offre);
            log.info("Offer validation status updated for ID: {} - New status: {}", offerId, isValid);

            return convertToResponse(updatedOffre);

        } catch (OffreNotFoundException | SecurityException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected validation error: {}", e.getMessage());
            throw new RuntimeException("Erreur inattendue lors de la validation de l'offre", e);
        }
    }

    public OffreDetailsResponse getOfferDetailsById(Long id) {
        Offre offre = offerRepository.findById(id)
                .orElseThrow(() -> new OffreNotFoundException("Offre non trouvée"));

        Patisserie patisserie = offre.getPatisserie();
        PatisserieResponse patisserieResponse = PatisserieResponse.builder()
                .id(patisserie.getId())
                .shopName(patisserie.getShopName())
                .phoneNumber(patisserie.getPhoneNumber())
                .location(patisserie.getLocation())
                .profilePicture(patisserie.getProfilePicture())
                .siretNumber(patisserie.getSiretNumber())
                .userEmail(patisserie.getUser().getEmail())
                .nombreOffres(patisserie.getOffres().size())
                .isValid(patisserie.isValid())
                .build();

        return OffreDetailsResponse.builder()
                .id(offre.getId())
                .typeEvenement(offre.getTypeEvenement())
                .kilos(offre.getKilos())
                .prix(offre.getPrix())
                .photoUrl(offre.getPhoto())
                .ville(offre.getVille())
                .valide(offre.estValide())
                .validatedByAdminId(offre.getAdmin() != null ? offre.getAdmin().getId() : null)
                .validatedByAdminName(offre.getAdmin() != null ? offre.getAdmin().getEmail() : null)
                .patisserie(patisserieResponse)
                .build();
    }

    public List<OffreResponse> getOffersByVille(String ville) {
        try {
            return offerRepository.findByVilleIgnoreCase(ville).stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting offers by ville: {}", e.getMessage());
            throw new OffreNotFoundException("Erreur lors de la recherche des offres par ville");
        }
    }

    public Page<OffreResponse> getOffersByVillePaginated(String ville, int page, int size) {
        try {
            return offerRepository.findByVilleIgnoreCase(ville,
                    PageRequest.of(page, size, Sort.by("prix").ascending()))
                    .map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Error getting paginated offers by ville: {}", e.getMessage());
            throw new OffreNotFoundException("Erreur lors de la recherche paginée des offres par ville");
        }
    }

    public List<OffreResponse> getOffersByVilleAndPatisserie(String ville, Long patisserieId) {
        try {
            if (!patisserieRepository.existsById(patisserieId)) {
                throw new OffreNotFoundException("Pâtisserie introuvable");
            }
            return offerRepository.findByVilleIgnoreCaseAndPatisserie_Id(ville, patisserieId).stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (OffreNotFoundException e) {
            log.error("Error getting offers by ville and patisserie: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting offers by ville and patisserie: {}", e.getMessage());
            throw new OffreNotFoundException("Erreur inattendue lors de la recherche des offres par ville et pâtisserie");
        }
    }

    private OffreResponse convertToResponse(Offre offre) {
        return OffreResponse.builder()
                .id(offre.getId())
                .typeEvenement(offre.getTypeEvenement())
                .kilos(offre.getKilos())
                .prix(offre.getPrix())
                .photoUrl(offre.getPhoto())
                .ville(offre.getVille())
                .valide(offre.getValide())
                .patisserieId(offre.getPatisserie().getId())
                .patisserieNom(offre.getPatisserie().getShopName())
                .validatedByAdminId(offre.getAdmin() != null ? offre.getAdmin().getId() : null)
                .validatedByAdminName(offre.getAdmin() != null ? offre.getAdmin().getEmail() : null)
                .createdAt(offre.getCreatedAt())
                .build();
    }
}