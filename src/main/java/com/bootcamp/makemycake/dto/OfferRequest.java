package com.bootcamp.makemycake.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class OfferRequest {

    @NotBlank(message = "Le type d'événement est obligatoire")
    @Size(max = 100, message = "Le type d'événement ne doit pas dépasser 100 caractères")
    private String typeEvenement;

    @NotNull(message = "Le poids est obligatoire")
    @DecimalMin(value = "0.1", message = "Le poids doit être d'au moins 0.1 kg")
    @DecimalMax(value = "50.0", message = "Le poids ne peut excéder 50 kg")
    private Double kilos;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "5.0", message = "Le prix doit être d'au moins 5€")
    @DecimalMax(value = "1000.0", message = "Le prix ne peut excéder 1000€")
    private Double prix;

    @NotNull(message = "La photo est obligatoire")
    @JsonIgnore // Empêche la sérialisation du fichier
    private MultipartFile photo;

    @NotNull(message = "L'ID de la pâtisserie est obligatoire")
    private Long patisserieId;

    // Méthode utilitaire pour le debug
    @Override
    public String toString() {
        return "OfferRequest{" +
                "typeEvenement='" + typeEvenement + '\'' +
                ", kilos=" + kilos +
                ", prix=" + prix +
                ", photo=" + (photo != null ? photo.getOriginalFilename() : "null") +
                ", patisserieId=" + patisserieId +
                '}';
    }

    // Méthode pour construire un builder
    public static OfferRequestBuilder builder() {
        return new OfferRequestBuilder();
    }

    // Builder interne
    public static class OfferRequestBuilder {
        private String typeEvenement;
        private Double kilos;
        private Double prix;
        private MultipartFile photo;
        private Long patisserieId;

        public OfferRequestBuilder typeEvenement(String typeEvenement) {
            this.typeEvenement = typeEvenement;
            return this;
        }

        public OfferRequestBuilder kilos(Double kilos) {
            this.kilos = kilos;
            return this;
        }

        public OfferRequestBuilder prix(Double prix) {
            this.prix = prix;
            return this;
        }

        public OfferRequestBuilder photo(MultipartFile photo) {
            this.photo = photo;
            return this;
        }

        public OfferRequestBuilder patisserieId(Long patisserieId) {
            this.patisserieId = patisserieId;
            return this;
        }

        public OfferRequest build() {
            OfferRequest request = new OfferRequest();
            request.setTypeEvenement(typeEvenement);
            request.setKilos(kilos);
            request.setPrix(prix);
            request.setPhoto(photo);
            request.setPatisserieId(patisserieId);
            return request;
        }
    }
}