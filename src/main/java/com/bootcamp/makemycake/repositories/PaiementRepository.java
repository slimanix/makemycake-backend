package com.bootcamp.makemycake.repositories;

import com.bootcamp.makemycake.entities.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
}