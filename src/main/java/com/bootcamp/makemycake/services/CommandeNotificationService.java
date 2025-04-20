package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.dto.CommandeDto;
import com.bootcamp.makemycake.entities.PatisseriePushSubscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class CommandeNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(CommandeNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final PushNotificationService pushNotificationService;
    private final ObjectMapper objectMapper;
    private PushService pushService;

    @Value("${vapid.private.key}")
    private String vapidPrivateKey;

    @Value("${vapid.public.key}")
    private String vapidPublicKey;

    @Value("${vapid.subject:mailto:contact@makemycake.com}")
    private String vapidSubject;

    @PostConstruct
    public void init() {
        try {
            // Enregistrer explicitement le provider BouncyCastle
            logger.info("Initializing Push Service...");
            if (Security.getProvider("BC") == null) {
                logger.debug("Adding BouncyCastle provider");
                Security.addProvider(new BouncyCastleProvider());
            }

            this.pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
            logger.info("Push Service initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize PushService", e);
            throw new RuntimeException("Failed to initialize PushService", e);
        }
    }

    public void notifierNouvelleCommande(Long patisserieId, CommandeDto commande) {
        logger.info("Notifying new order {} for patisserie {}", commande.getId(), patisserieId);

        // Notification WebSocket
        try {
            String destination = "/topic/patisserie/" + patisserieId + "/nouvelles-commandes";
            logger.debug("Sending WebSocket notification to {}", destination);
            messagingTemplate.convertAndSend(destination, commande);
            logger.debug("WebSocket notification sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification", e);
        }

        // Notification Push
        try {
            sendPushNotification(patisserieId, commande);
        } catch (Exception e) {
            logger.error("Failed to send push notification", e);
        }
    }

    private void sendPushNotification(Long patisserieId, CommandeDto commande) {
        logger.info("Preparing push notifications for patisserie ID: {}", patisserieId);

        List<PatisseriePushSubscription> subscriptions = pushNotificationService.getSubscriptionsForPatisserie(patisserieId);

        if (subscriptions.isEmpty()) {
            logger.info("No push subscriptions found for patisserie ID: {}", patisserieId);
            return;
        }

        logger.info("Found {} push subscriptions", subscriptions.size());

        String title = "Nouvelle commande #" + commande.getId();
        String body = "Montant: " + commande.getMontantTotal() + "€ - " + commande.getPatisserieNom();

        Map<String, Object> data = new HashMap<>();
        data.put("commandeId", commande.getId());
        data.put("url", "/patissier/commandes");

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("body", body);
        payload.put("data", data);
        payload.put("icon", "/logo.png");
        payload.put("badge", "/logo.png");
        payload.put("requireInteraction", true);
        payload.put("actions", new Object[]{
                Map.of("action", "view", "title", "Voir"),
                Map.of("action", "close", "title", "Fermer")
        });

        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            logger.debug("Push payload: {}", payloadJson);

            for (PatisseriePushSubscription subscription : subscriptions) {
                try {
                    logger.debug("Sending push notification to endpoint: {}", subscription.getEndpoint());

                    Notification notification = new Notification(
                            subscription.getEndpoint(),
                            subscription.getP256dh(),
                            subscription.getAuth(),
                            payloadJson
                    );

                    pushService.send(notification);
                    logger.info("Push notification sent successfully to endpoint: {}", subscription.getEndpoint());
                } catch (GeneralSecurityException | IOException | JoseException | ExecutionException | InterruptedException e) {
                    logger.error("Failed to send push notification to endpoint: " + subscription.getEndpoint(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error preparing push notification payload", e);
        }
    }
}