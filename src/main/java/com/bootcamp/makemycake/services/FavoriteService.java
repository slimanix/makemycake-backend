package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.entities.Client;
import com.bootcamp.makemycake.entities.Favorite;
import com.bootcamp.makemycake.entities.Offre;
import com.bootcamp.makemycake.exceptions.favorite.FavoriteNotFoundException;
import com.bootcamp.makemycake.repositories.ClientRepository;
import com.bootcamp.makemycake.repositories.FavoriteRepository;
import com.bootcamp.makemycake.repositories.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ClientRepository clientRepository;
    private final OfferRepository offerRepository;

    @Transactional(readOnly = true)
    public List<Favorite> getClientFavorites(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new FavoriteNotFoundException("Client not found");
        }
        return favoriteRepository.findByClientId(clientId);
    }

    @Transactional
    public Favorite addToFavorites(Long clientId, Long offerId) {
        // Check if client and offer exist
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new FavoriteNotFoundException("Client not found"));
        
        Offre offre = offerRepository.findById(offerId)
                .orElseThrow(() -> new FavoriteNotFoundException("Offer not found"));

        // Check if favorite already exists
        Optional<Favorite> existingFavorite = favoriteRepository.findByClientIdAndOffreId(clientId, offerId);
        
        // If favorite exists, return it without creating a new one
        if (existingFavorite.isPresent()) {
            return existingFavorite.get();
        }

        // Create new favorite if it doesn't exist
        Favorite favorite = Favorite.builder()
                .client(client)
                .offre(offre)
                .build();

        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFromFavorites(Long clientId, Long offerId) {
        // Check if client and offer exist
        if (!clientRepository.existsById(clientId)) {
            throw new FavoriteNotFoundException("Client not found");
        }
        if (!offerRepository.existsById(offerId)) {
            throw new FavoriteNotFoundException("Offer not found");
        }

        // Try to delete the favorite, but don't throw an exception if it doesn't exist
        favoriteRepository.deleteByClientIdAndOffreId(clientId, offerId);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long clientId, Long offerId) {
        if (!clientRepository.existsById(clientId)) {
            throw new FavoriteNotFoundException("Client not found");
        }
        if (!offerRepository.existsById(offerId)) {
            throw new FavoriteNotFoundException("Offer not found");
        }
        return favoriteRepository.findByClientIdAndOffreId(clientId, offerId).isPresent();
    }
} 