package com.bootcamp.makemycake.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PaiementRequest {
    @NotBlank
    @Pattern(regexp = "^[0-9]{16}$")
    private String numeroCarte;

    @NotBlank
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$")
    private String dateExpiration;

    @NotBlank
    @Pattern(regexp = "^[0-9]{3}$")
    private String cvv;
}