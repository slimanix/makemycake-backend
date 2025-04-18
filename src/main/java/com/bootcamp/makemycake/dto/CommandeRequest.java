package com.bootcamp.makemycake.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class CommandeRequest {
    @NotNull
    private Long patisserieId;

    @NotNull @Min(1)
    private Integer nombrePersonnes;

    @NotBlank
    private String glacage;

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