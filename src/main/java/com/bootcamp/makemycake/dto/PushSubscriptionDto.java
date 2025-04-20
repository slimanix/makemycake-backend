package com.bootcamp.makemycake.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PushSubscriptionDto {
    @NotNull
    private Long patisserieId;

    @NotNull
    private Subscription subscription;

    @Data
    public static class Subscription {
        @NotBlank
        private String endpoint;

        @NotNull
        private Keys keys;
    }

    @Data
    public static class Keys {
        @NotBlank
        private String p256dh;

        @NotBlank
        private String auth;
    }
}