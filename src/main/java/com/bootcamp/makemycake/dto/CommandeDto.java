package com.bootcamp.makemycake.dto;

import com.bootcamp.makemycake.entities.StatutCommande;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class CommandeDto {
    private Long id;
    private LocalDateTime dateCreation;
    private Double montantTotal;
    private StatutCommande statut;
    private Integer nombrePersonnes;
    private String glacage;
    private String telephoneClient;
    private Long patisserieId;
    private String patisserieNom;
    private List<CoucheDto> couches;

    @Getter @Setter
    public static class CoucheDto {
        private String saveur;
        private Integer epaisseur;
        private Double prix;
    }
}