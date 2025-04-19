package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.dto.CommandeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandeNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifierNouvelleCommande(Long patisserieId, CommandeDto commande) {
        messagingTemplate.convertAndSend(
                "/topic/patisserie/" + patisserieId + "/nouvelles-commandes",
                commande
        );
    }
}