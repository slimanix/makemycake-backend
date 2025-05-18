package com.bootcamp.makemycake.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "offres")  // Nom de la table en base de données
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "type_evenement", nullable = false)
    private String typeEvenement;  // Ex: Anniversaire, Mariage, etc.

    @Column(nullable = false)
    private Double kilos;  // Poids du gâteau en kilos

    @Column(nullable = false)
    private Double prix;   // Prix en euros (ou autre devise)

    @Column(name = "ville", nullable = false, length = 255)
    private String ville;  // Ville où l'offre est disponible

    @Column(name = "photo_url")
    private String photo;  // URL ou chemin de la photo

    @Column(name = "is_valide", nullable = false)
    private Boolean valide = false;  // Par défaut, l'offre n'est pas validée par l'admin

    // Relation Many-to-One avec la pâtisserie (une pâtisserie peut avoir plusieurs offres)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patisserie_id", nullable = false)
    private Patisserie patisserie;

    // Relation Many-to-One avec l'admin qui a validé l'offre (optionnelle)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;  // Supposant que l'admin est un User avec rôle ADMIN

    // Méthode utilitaire pour vérifier si l'offre est validée
    public boolean estValide() {
        return Boolean.TRUE.equals(valide);
    }

}