package com.bootcamp.makemycake.repositories;

import com.bootcamp.makemycake.entities.User;
import com.bootcamp.makemycake.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByActivationToken(String token);

    // Role-specific queries
    List<User> findByRole(UserRole role);

    // Combined activation status query
    @Query("SELECT u FROM User u WHERE u.enabled = :enabled AND u.role = :role")
    List<User> findByEnabledAndRole(boolean enabled, UserRole role);

    // Exists by email and role
    boolean existsByEmailAndRole(String email, UserRole role);
}