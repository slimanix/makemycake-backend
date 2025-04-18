package com.bootcamp.makemycake.repositories;

import com.bootcamp.makemycake.entities.Couche;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoucheRepository extends JpaRepository<Couche, Long> {
}