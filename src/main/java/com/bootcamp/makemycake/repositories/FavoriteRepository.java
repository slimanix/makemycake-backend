package com.bootcamp.makemycake.repositories;

import com.bootcamp.makemycake.entities.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByClientId(Long clientId);
    Optional<Favorite> findByClientIdAndOffreId(Long clientId, Long offreId);
    void deleteByClientIdAndOffreId(Long clientId, Long offreId);
} 