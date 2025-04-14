package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.*;
import com.bootcamp.makemycake.repositories.UserRepository;
import com.bootcamp.makemycake.services.AuthService;
import com.bootcamp.makemycake.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController // Cette annotation permet à Spring de reconnaître cette classe comme un contrôleur REST. Elle traite les requêtes HTTP et renvoie des réponses au format JSON ou d'autres formats.
@RequestMapping("/auth") // Définit la route de base pour toutes les méthodes de ce contrôleur. Toutes les requêtes commenceront par "/auth", suivies de la méthode spécifique.
public class AuthController {


    private final AuthService authService; // Service qui gère l'authentification (login, registration, activation, etc.)
    private final UserRepository userRepository; // Référentiel pour interagir avec la base de données des utilisateurs.
    private final EmailService emailService; // Service pour l'envoi d'e-mails, utilisé pour envoyer des liens d'activation et de réinitialisation de mot de passe.
    private final PasswordEncoder passwordEncoder; // Encodage du mot de passe pour le stockage sécurisé des mots de passe des utilisateurs.

    public AuthController(AuthService authService, UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.authService = authService; // Initialisation de l'authentification service.
        this.userRepository = userRepository; // Initialisation du repository utilisateur.
        this.emailService = emailService; // Initialisation du service d'email.
        this.passwordEncoder = passwordEncoder; // Initialisation du service d'encodage de mot de passe.
    }

    @PostMapping("/login") // Cette annotation définit une route HTTP POST pour la connexion.
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {

        String token = authService.login(loginRequest);

        return ResponseEntity.ok(new ApiResponse(token, "Success", 200)); // 200 est le code HTTP de succès.
    }

    @PostMapping("/register") // Cette annotation définit une route HTTP POST pour l'enregistrement d'un nouvel utilisateur.
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) throws Exception {

        ApiResponse<String> response = authService.register(request);

        return ResponseEntity.ok(response);  // Code HTTP 200, avec la réponse d'inscription.
    }

    @GetMapping("/activate") // Cette annotation définit une route HTTP GET pour activer le compte de l'utilisateur via un token.
    public ResponseEntity<ApiResponse<String>> activateAccount(@RequestParam String token) {

        ApiResponse<String> response = authService.activateAccount(token);

        return ResponseEntity.ok(response);  // Code HTTP 200 avec message de succès.
    }

    @PostMapping("/forgot-password") // Cette annotation définit une route HTTP POST pour la demande de réinitialisation de mot de passe.
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
        } catch (Exception e) {
            throw new RuntimeException(e); // Exception pour interrompre l'exécution.
        }

        return ResponseEntity.ok(new ApiResponse<>("Email de réinitialisation envoyé !", HttpStatus.OK.value()));
    }

    @PostMapping("/reset-password") // Cette annotation définit une route HTTP POST pour réinitialiser le mot de passe.
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok(new ApiResponse<>("Mot de passe réinitialisé avec succès !", HttpStatus.OK.value()));
    }
}
