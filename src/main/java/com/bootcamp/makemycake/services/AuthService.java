package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.dto.*;
import com.bootcamp.makemycake.entities.*;
import com.bootcamp.makemycake.exceptions.auth.*;
import com.bootcamp.makemycake.exceptions.email.SendingEmailException;
import com.bootcamp.makemycake.repositories.*;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bootcamp.makemycake.security.JwtUtils;  // Correct import

import java.util.*;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PatisserieRepository patisserieRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;

    @Transactional
    public ApiResponse<String> register(RegisterRequest request) throws Exception {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Un utilisateur avec cet email existe déjà.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);
        user.setActivationToken(UUID.randomUUID().toString());
        user.setRole(request.getRole()); // Utilisation directe de l'enum UserRole

        User savedUser = userRepository.save(user);

        // Création du profil spécifique sans changer la logique
        if (request.getRole() == UserRole.CLIENT) {
            Client client = new Client();
            client.setUser(savedUser);
            client.setFullName(request.getFullName());
            client.setPhoneNumber(request.getPhoneNumber());
            client.setAddress(request.getAddress());
            clientRepository.save(client);
        } else if (request.getRole() == UserRole.PATISSIER) {
            Patisserie patisserie = new Patisserie();
            patisserie.setUser(savedUser);
            patisserie.setShopName(request.getShopName());
            patisserie.setPhoneNumber(request.getPhoneNumber());
            patisserie.setLocation(request.getLocation());
            patisserie.setSiretNumber(request.getSiretNumber());
            patisserieRepository.save(patisserie);
        }

        String activationLink = "http://localhost:8080/auth/activate?token=" + user.getActivationToken();
        Map<String, String> emailVariables = Map.of("activationLink", activationLink);
        String emailContent = emailService.loadEmailTemplate("templates/emails/activation-email.html", emailVariables);

        try {
            emailService.sendEmail(user.getEmail(), "Activation de votre compte", emailContent);
        } catch (MessagingException e) {
            throw new SendingEmailException("Erreur lors de l'envoi de l'email d'activation.");
        }

        return new ApiResponse<>("Inscription réussie ! Vérifiez votre email.", HttpStatus.OK.value());
    }

    public String login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        if (authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("Identifiants invalides."));

            if (!user.isEnabled()) {
                throw new AccountIsNotEnabledException("Compte non activé. Vérifiez votre email.");
            }
            return jwtUtils.generateToken(authentication);
        }
        throw new InvalidCredentialsException("Identifiants invalides.");
    }

    public ApiResponse<String> activateAccount(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token invalide !"));

        user.setEnabled(true);
        user.setActivationToken(null);
        userRepository.save(user);

        return new ApiResponse<>("Compte activé avec succès !", HttpStatus.OK.value());
    }

    public void forgotPassword(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("Email non trouvé !"));

        String resetToken = UUID.randomUUID().toString();
        user.setActivationToken(resetToken);
        userRepository.save(user);

        String resetLink = "http://localhost:8080/auth/reset-password?token=" + resetToken;
        Map<String, String> emailVariables = Map.of("resetLink", resetLink);
        String emailContent = emailService.loadEmailTemplate("templates/emails/reset-password-email.html", emailVariables);

        emailService.sendEmail(user.getEmail(), "Réinitialisation du mot de passe", emailContent);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token invalide !"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setActivationToken(null);
        userRepository.save(user);
    }
}