package com.bootcamp.makemycake.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PatisserieUpdateRequest {
    @NotBlank(message = "Le nom de la boutique est requis")
    @Size(min = 2, max = 100, message = "Le nom de la boutique doit contenir entre 2 et 100 caractères")
    private String shopName;

    @NotBlank(message = "Le numéro de téléphone est requis")
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Format de numéro de téléphone invalide")
    private String phoneNumber;

    @NotBlank(message = "La localisation est requise")
    @Size(min = 2, max = 100, message = "La localisation doit contenir entre 2 et 100 caractères")
    private String location;

    private MultipartFile banner;
} 