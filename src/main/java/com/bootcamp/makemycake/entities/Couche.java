package com.bootcamp.makemycake.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "couches")
@Getter @Setter
public class Couche {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String saveur;

    @Column(nullable = false)
    private Integer epaisseur; // 1-3

    @Column(nullable = false)
    private Double prix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;
}