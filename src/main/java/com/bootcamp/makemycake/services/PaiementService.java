package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.dto.PaiementDto;
import com.bootcamp.makemycake.dto.PaiementRequest;
import com.bootcamp.makemycake.entities.Commande;
import com.bootcamp.makemycake.entities.Paiement;
import com.bootcamp.makemycake.entities.StatutCommande;
import com.bootcamp.makemycake.exceptions.paiement.NotFoundException;
import com.bootcamp.makemycake.repositories.CommandeRepository;
import com.bootcamp.makemycake.repositories.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;

    @Transactional
    public PaiementDto processPaiement(Long commandeId, PaiementRequest request) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new NotFoundException("Commande non trouvée"));

        Paiement paiement = new Paiement();
        paiement.setCommande(commande);
        paiement.setMontant(commande.getMontantTotal());
        paiement.setMethode("CB");
        paiement.setStatut("COMPLETEE");

        commande.setStatut(StatutCommande.PREPARATION);
        commandeRepository.save(commande);

        Paiement saved = paiementRepository.save(paiement);
        return convertToDto(saved);
    }

    private PaiementDto convertToDto(Paiement paiement) {
        PaiementDto dto = new PaiementDto();
        dto.setId(paiement.getId());
        dto.setMontant(paiement.getMontant());
        dto.setMethode(paiement.getMethode());
        dto.setStatut(paiement.getStatut());
        dto.setDatePaiement(paiement.getDatePaiement());
        return dto;
    }
}