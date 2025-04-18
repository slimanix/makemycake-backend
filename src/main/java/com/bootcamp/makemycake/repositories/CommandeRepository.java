package com.bootcamp.makemycake.repositories;

import com.bootcamp.makemycake.entities.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {
    @Query("SELECT c FROM Commande c WHERE c.client.id = :clientId")
    List<Commande> findByClientId(Long clientId);

    @Query("SELECT c FROM Commande c WHERE c.patisserie.id = :patisserieId")
    List<Commande> findByPatisserieId(Long patisserieId);

    @Query("SELECT c FROM Commande c WHERE c.statut = 'EN_ATTENTE' AND c.patisserie.id = :patisserieId")
    List<Commande> findPendingByPatisserieId(Long patisserieId);
}