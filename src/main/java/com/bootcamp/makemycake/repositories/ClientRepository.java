package com.bootcamp.makemycake.repositories;

import com.bootcamp.makemycake.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    // Find by user email (through the relationship)
    @Query("SELECT c FROM Client c JOIN c.user u WHERE u.email = :email")
    Optional<Client> findByUserEmail(String email);

    // Find all clients with enabled accounts
    @Query("SELECT c FROM Client c JOIN c.user u WHERE u.enabled = true")
    List<Client> findAllEnabledClients();

    // Search clients by name
    List<Client> findByFullNameContainingIgnoreCase(String name);
}