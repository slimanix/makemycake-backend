package com.bootcamp.makemycake.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class CommandeRequest {
    @NotNull(message = "L'ID de la pâtisserie est requis")
    private Long patisserieId;

    @NotNull(message = "L'ID de l'offre est requis")
    private Long offerId;

    @NotNull @Min(1)
    private Integer nombrePersonnes;

    @NotBlank(message = "Le glacage est requis")
    private String glacage;

    @NotBlank(message = "Le numéro de téléphone est requis")
    @Pattern(regexp = "^\\+?[0-9]{8,15}$")
    private String telephone;

    @NotEmpty
    private List<CoucheRequest> couches;

    @Getter @Setter
    public static class CoucheRequest {
        @NotBlank
        private String saveur;

        @Min(1) @Max(3)
        private Integer epaisseur;
    }
}