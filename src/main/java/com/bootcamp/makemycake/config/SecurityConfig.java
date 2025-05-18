package com.bootcamp.makemycake.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.bootcamp.makemycake.security.JwtAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // <-- Ajoutez cette ligne
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/auth/**",
                                "/api/patisseries/valides",
                                "/api/patisseries/{id}",
                                "api/offres/patisserie/{patisserieId}",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/offres",          // GET public
                                "/api/offres/{id}",     // GET public
                                "/api/offres/patisserie/**", // GET public
                                "/websocket-test.html",  // Autorisez explicitement cette page
                                "/sw.js",
                                "/api/push/**",  // <-- Ajoutez cette ligne
                                "/logo.png",
                                "/static/**",           // Autorisez le dossier static
                                "/ws-commandes/**",     // Autorisez les WebSockets
                                "/error",
                                "/api/cities"                // Autorisez la page d'erreur
                        ).permitAll()

                        // Client endpoints
                        .requestMatchers(
                                "/api/offres",          // POST
                                "/api/client/**",
                                "/api/commandes",       // POST commande
                                "/api/commandes/client" // GET commandes client
                        ).hasRole("CLIENT")

                        // Patissier endpoints - Updated section
                        .requestMatchers(
                                "/api/patissier/**",
                                "/api/offres/my-offers",
                                "/api/commandes**"      // Now matches with or without query params
                        ).hasRole("PATISSIER")

                        // Payment endpoints
                        .requestMatchers(
                                "/api/commandes/*/paiement"
                        ).hasRole("CLIENT")

                        // Add this to your existing requestMatchers for patissier endpoints
                        .requestMatchers(HttpMethod.PUT, "/api/commandes/*/statut")
                        .hasAnyRole("PATISSIER", "CLIENT")

                        // Admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/offres/*/validate").hasRole("ADMIN")

                        // New request matcher for PATCH /api/patisseries/*
                        .requestMatchers(HttpMethod.PATCH, "/api/patisseries/*").hasRole("PATISSIER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization")); // Important pour JWT
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}