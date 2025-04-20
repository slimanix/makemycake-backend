package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.dto.PushSubscriptionDto;
import com.bootcamp.makemycake.entities.PatisseriePushSubscription;
import com.bootcamp.makemycake.repositories.PatisseriePushSubscriptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

    private final PatisseriePushSubscriptionRepository subscriptionRepository;

    @Transactional
    public void saveSubscription(PushSubscriptionDto dto) {
        logger.info("Saving push subscription for patisserie ID: {}", dto.getPatisserieId());

        // Validation supplémentaire
        if (dto.getPatisserieId() == null ||
                dto.getSubscription() == null ||
                dto.getSubscription().getEndpoint() == null ||
                dto.getSubscription().getKeys() == null ||
                dto.getSubscription().getKeys().getP256dh() == null ||
                dto.getSubscription().getKeys().getAuth() == null) {
            logger.error("Invalid subscription data: {}", dto);
            throw new IllegalArgumentException("Invalid subscription data");
        }

        try {
            // Supprimer les anciens abonnements pour cette pâtisserie
            logger.debug("Deleting existing subscriptions for patisserie ID: {}", dto.getPatisserieId());
            subscriptionRepository.deleteByPatisserieId(dto.getPatisserieId());

            // Sauvegarder le nouvel abonnement
            PatisseriePushSubscription subscription = new PatisseriePushSubscription();
            subscription.setPatisserieId(dto.getPatisserieId());
            subscription.setEndpoint(dto.getSubscription().getEndpoint());
            subscription.setP256dh(dto.getSubscription().getKeys().getP256dh());
            subscription.setAuth(dto.getSubscription().getKeys().getAuth());

            logger.debug("Saving new subscription with endpoint: {}", dto.getSubscription().getEndpoint());
            PatisseriePushSubscription saved = subscriptionRepository.save(subscription);
            logger.info("Successfully saved subscription with ID: {}", saved.getId());
        } catch (Exception e) {
            logger.error("Error saving subscription", e);
            throw new RuntimeException("Failed to save push subscription", e);
        }
    }

    public List<PatisseriePushSubscription> getSubscriptionsForPatisserie(Long patisserieId) {
        logger.info("Getting push subscriptions for patisserie ID: {}", patisserieId);
        List<PatisseriePushSubscription> subscriptions = subscriptionRepository.findByPatisserieId(patisserieId);
        logger.info("Found {} subscriptions for patisserie ID: {}", subscriptions.size(), patisserieId);
        return subscriptions;
    }
}