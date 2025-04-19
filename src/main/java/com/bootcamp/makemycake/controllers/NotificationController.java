package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.CommandeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class NotificationController {
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyNewCommande(Long patisserieId, CommandeDto commandeDto) {
        messagingTemplate.convertAndSend("/topic/patisserie/" + patisserieId + "/commandes", commandeDto);
    }
}
