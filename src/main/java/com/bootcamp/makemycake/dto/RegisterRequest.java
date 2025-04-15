package com.bootcamp.makemycake.dto;

import com.bootcamp.makemycake.entities.UserRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @Email @NotBlank
    private String email;

    @Size(min = 8)
    private String password;

    @NotNull
    private UserRole role;

    // Champs client
    private String fullName;
    private String phoneNumber;
    private String address;

    // Champs patissier
    private String shopName;
    private String location;
    private String siretNumber;
    private boolean validated;
    private  boolean enabled;
}