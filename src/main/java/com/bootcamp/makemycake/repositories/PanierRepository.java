package com.bootcamp.makemycake.repositories;

import com.bootcamp.makemycake.entities.Panier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface PanierRepository extends JpaRepository<Panier, Long> {
    @Query("SELECT p FROM Panier p WHERE p.client.id = :clientId")
    Optional<Panier> findByClientId(Long clientId);
}