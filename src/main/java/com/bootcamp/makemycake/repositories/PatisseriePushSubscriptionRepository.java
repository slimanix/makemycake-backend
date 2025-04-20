package com.bootcamp.makemycake.repositories;

import com.bootcamp.makemycake.entities.PatisseriePushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatisseriePushSubscriptionRepository extends JpaRepository<PatisseriePushSubscription, Long> {

    List<PatisseriePushSubscription> findByPatisserieId(Long patisserieId);

    void deleteByPatisserieId(Long patisserieId);
}