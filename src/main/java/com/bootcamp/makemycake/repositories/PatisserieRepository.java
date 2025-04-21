package com.bootcamp.makemycake.repositories;

import com.bootcamp.makemycake.entities.Patisserie;
import org.eclipse.angus.mail.imap.protocol.ID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PatisserieRepository extends JpaRepository<Patisserie, Long> {
    // Find by user email
    @Query("SELECT p FROM Patisserie p JOIN p.user u WHERE u.email = :email")
    public Optional<Patisserie> findByUserEmail(String email);

    // Find by location
    List<Patisserie> findByLocationContainingIgnoreCase(String location);

    // Find by shop name
    List<Patisserie> findByShopNameContainingIgnoreCase(String shopName);

    public Optional<Patisserie> findById(Long id);  // Existe par défaut  // Déjà disponible grâce à JpaRepository

    // Find all verified patisseries (with SIRET number)
    @Query("SELECT p FROM Patisserie p WHERE p.isValid = true")
    List<Patisserie> findAllVerifiedPatisseries();

    @Query("SELECT p FROM Patisserie p WHERE p.isValid = false")
    List<Patisserie> findAllNonValidatedPatisseries();

}