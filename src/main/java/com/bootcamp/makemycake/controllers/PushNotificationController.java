package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.PushSubscriptionDto;
import com.bootcamp.makemycake.services.PushNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationController.class);
    private final PushNotificationService pushNotificationService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@Valid @RequestBody PushSubscriptionDto subscription) {
        logger.info("Received push subscription request for patisserie ID: {}",
                subscription != null ? subscription.getPatisserieId() : "null");

        try {
            // Validation des données
            if (subscription == null || subscription.getSubscription() == null ||
                    subscription.getSubscription().getKeys() == null) {
                logger.error("Invalid subscription data received");
                Map<String, String> response = new HashMap<>();
                response.put("error", "Invalid subscription data");
                return ResponseEntity.badRequest().body(response);
            }

            // Log pour le débogage
            logger.debug("Processing subscription with endpoint: {}",
                    subscription.getSubscription().getEndpoint());

            pushNotificationService.saveSubscription(subscription);

            // Réponse de succès plus détaillée
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Subscription saved successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Exception de validation
            logger.error("Validation error for subscription", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            // Autres exceptions
            logger.error("Error processing subscription", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Server error occurred while processing subscription");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint de test pour les notifications push
    @PostMapping("/test-notification/{patisserieId}")
    public ResponseEntity<?> testNotification(@PathVariable Long patisserieId) {
        logger.info("Testing push notification for patisserie ID: {}", patisserieId);

        try {
            // Créer une fausse commande pour test
            com.bootcamp.makemycake.dto.CommandeDto testCommande = new com.bootcamp.makemycake.dto.CommandeDto();
            testCommande.setId(999L); // ID fictif pour le test
            testCommande.setMontantTotal(99.99);
            testCommande.setPatisserieId(patisserieId);
            testCommande.setPatisserieNom("Test Patisserie");

            // Appeler le service de notification
            pushNotificationService.getSubscriptionsForPatisserie(patisserieId).size();

            // Réponse
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Test notification sent");
            response.put("subscriptionsCount", pushNotificationService.getSubscriptionsForPatisserie(patisserieId).size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending test notification", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error sending test notification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}