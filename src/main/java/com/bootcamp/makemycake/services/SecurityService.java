package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.entities.Patisserie;
import com.bootcamp.makemycake.entities.User;
import com.bootcamp.makemycake.repositories.PatisserieRepository;
import com.bootcamp.makemycake.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    private final PatisserieRepository patisserieRepository;
    private final UserRepository userRepository;

    public SecurityService(PatisserieRepository patisserieRepository, UserRepository userRepository) {
        this.patisserieRepository = patisserieRepository;
        this.userRepository = userRepository;
    }

    public boolean isPatisserieOwner(Long patisserieId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Patisserie patisserie = patisserieRepository.findById(patisserieId).orElse(null);
        if (patisserie == null) return false;
        User user = patisserie.getUser();
        return user != null && user.getEmail().equals(username);
    }
} 