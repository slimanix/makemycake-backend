package com.bootcamp.makemycake.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Table(name = "patisseries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)


public class Patisserie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // ← Ajoutez ceci

    private User user;

    @Column(name = "shop_name", nullable = false)
    private String shopName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String location;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "siret_number")
    private String siretNumber;

    private boolean validated = false;

    // New: One-to-Many relationship with Offre
    @OneToMany(mappedBy = "patisserie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Offre> offres;  // All offers created by this patisserie

    @Column(name = "is_valid", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isValid = false; // Par défaut non validé

}
