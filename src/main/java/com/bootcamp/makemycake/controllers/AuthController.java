package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.*;
import com.bootcamp.makemycake.entities.*;
import com.bootcamp.makemycake.exceptions.auth.InvalidTokenException;
import com.bootcamp.makemycake.repositories.*;
import com.bootcamp.makemycake.services.AuthService;
import com.bootcamp.makemycake.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController // Cette annotation permet à Spring de reconnaître cette classe comme un contrôleur REST. Elle traite les requêtes HTTP et renvoie des réponses au format JSON ou d'autres formats.
@RequestMapping("/auth") // Définit la route de base pour toutes les méthodes de ce contrôleur. Toutes les requêtes commenceront par "/auth", suivies de la méthode spécifique.
@CrossOrigin(origins = "http://localhost:4200") // Add this line to allow requests from Angular frontend
public class AuthController {

    private final AuthService authService; // Service qui gère l'authentification (login, registration, activation, etc.)
    private final UserRepository userRepository; // Référentiel pour interagir avec la base de données des utilisateurs.
    private final ClientRepository clientRepository;
    private final PatisserieRepository patisserieRepository;
    private final EmailService emailService; // Service pour l'envoi d'e-mails, utilisé pour envoyer des liens d'activation et de réinitialisation de mot de passe.
    private final PasswordEncoder passwordEncoder; // Encodage du mot de passe pour le stockage sécurisé des mots de passe des utilisateurs.

    public AuthController(AuthService authService, UserRepository userRepository, 
                         ClientRepository clientRepository, PatisserieRepository patisserieRepository,
                         EmailService emailService, PasswordEncoder passwordEncoder) {
        this.authService = authService; // Initialisation de l'authentification service.
        this.userRepository = userRepository; // Initialisation du repository utilisateur.
        this.clientRepository = clientRepository;
        this.patisserieRepository = patisserieRepository;
        this.emailService = emailService; // Initialisation du service d'email.
        this.passwordEncoder = passwordEncoder; // Initialisation du service d'encodage de mot de passe.
    }

    @PostMapping("/login") // Cette annotation définit une route HTTP POST pour la connexion.
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {

        String token = authService.login(loginRequest);

        return ResponseEntity.ok(new ApiResponse(token, "Success", 200)); // 200 est le code HTTP de succès.
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> register(
            @RequestPart("request") RegisterRequest request,  // Required JSON part
            @RequestPart(value = "profileImage", required = false) MultipartFile file  // Optional file
    ) throws Exception {
        ApiResponse<String> response = authService.register(request, file);
        return ResponseEntity.ok(response);
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

    @GetMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> validateResetToken(@RequestParam String token) {
        try {
            // Validate the token
            User user = userRepository.findByActivationToken(token)
                    .orElseThrow(() -> new InvalidTokenException("Token invalide !"));
            return ResponseEntity.ok(new ApiResponse<>("Token valide", HttpStatus.OK.value()));
        } catch (InvalidTokenException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse<>("Mot de passe réinitialisé avec succès !", HttpStatus.OK.value()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserInfoResponse.UserInfoResponseBuilder userInfoBuilder = UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .enabled(user.isEnabled());
        
        // Add role-specific information
        if (user.getRole() == UserRole.CLIENT) {
            Client client = clientRepository.findByUserEmail(email)
                    .orElseThrow(() -> new RuntimeException("Client information not found"));
            userInfoBuilder.clientInfo(ClientInfoResponse.builder()
                    .fullName(client.getFullName())
                    .phoneNumber(client.getPhoneNumber())
                    .address(client.getAddress())
                    .build());
        }  else if (user.getRole() == UserRole.PATISSIER) {
            Patisserie patisserie = patisserieRepository.findByUserEmail(email)
                    .orElseThrow(() -> new RuntimeException("Patisserie information not found"));
            userInfoBuilder.patisserieInfo(PatisserieInfoResponse.builder()
                    .id(patisserie.getId())
                    .shopName(patisserie.getShopName())
                    .phoneNumber(patisserie.getPhoneNumber())
                    .location(patisserie.getLocation())
                    .profilePicture(patisserie.getProfilePicture()) // ← THIS WAS MISSING
                    .siretNumber(patisserie.getSiretNumber())
                    .validated(patisserie.isValidated())
                    .isValid(patisserie.isValid())
                    .build());
        }
                
        return ResponseEntity.ok(new ApiResponse<>(userInfoBuilder.build(), 
            "User information retrieved successfully", HttpStatus.OK.value()));
    }
}
