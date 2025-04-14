package com.bootcamp.makemycake.repositories;

import com.bootcamp.makemycake.entities.Role;
import com.bootcamp.makemycake.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(UserRole name);

    // Additional useful query methods
    boolean existsByName(UserRole name);
}