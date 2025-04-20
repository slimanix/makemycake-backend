package com.bootcamp.makemycake.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patisserie_push_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatisseriePushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patisserie_id", nullable = false)
    private Long patisserieId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String endpoint;

    @Column(nullable = false)
    private String p256dh;

    @Column(nullable = false)
    private String auth;
}