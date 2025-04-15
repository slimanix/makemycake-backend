package com.bootcamp.makemycake.repositories;

import com.bootcamp.makemycake.entities.Patisserie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PatisserieRepository extends JpaRepository<Patisserie, Long> {
    // Find by user email
    @Query("SELECT p FROM Patisserie p JOIN p.user u WHERE u.email = :email")
    Optional<Patisserie> findByUserEmail(String email);

    // Find by location
    List<Patisserie> findByLocationContainingIgnoreCase(String location);

    // Find by shop name
    List<Patisserie> findByShopNameContainingIgnoreCase(String shopName);

    // Find all verified patisseries (with SIRET number)
    @Query("SELECT p FROM Patisserie p WHERE p.isValid = true")
    List<Patisserie> findAllVerifiedPatisseries();

    @Query("SELECT p FROM Patisserie p WHERE p.isValid = false")
    List<Patisserie> findAllNonValidatedPatisseries();
}