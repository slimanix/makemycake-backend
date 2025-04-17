package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.ApiResponse;
import com.bootcamp.makemycake.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patisseries")
public class PatisserieController {

    @Autowired
    private AuthService authService;

    @PatchMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<String>> validatePatisserie(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        // Ici vous pourriez ajouter une vérification du rôle ADMIN
        // avant de permettre la validation

        ApiResponse<String> response = authService.validatePatisserie(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}