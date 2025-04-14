package com.bootcamp.makemycake.repositories;

import com.bootcamp.makemycake.entities.Offre;
import com.bootcamp.makemycake.entities.Patisserie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offre, Long> {

    // Find all offers by patisserie
    List<Offre> findByPatisserie_Id(Long patisserieId);  // Notez le _ pour accéder à l'ID

    // Version paginée
    Page<Offre> findByPatisserieId(Long patisserieId, Pageable pageable);

    // Find all validated offers (where valide = true)
    List<Offre> findByValideTrue();

    // Find offers by event type (case-insensitive search)
    List<Offre> findByTypeEvenementIgnoreCase(String typeEvenement);

    // Custom query: Find offers within a price range
    @Query("SELECT o FROM Offre o WHERE o.prix BETWEEN :minPrice AND :maxPrice")
    List<Offre> findOffersInPriceRange(@Param("minPrice") double minPrice,
                                       @Param("maxPrice") double maxPrice);

    // Custom query: Count offers by patisserie
    @Query("SELECT COUNT(o) FROM Offre o WHERE o.patisserie = :patisserie")
    long countByPatisserie(@Param("patisserie") Patisserie patisserie);


}