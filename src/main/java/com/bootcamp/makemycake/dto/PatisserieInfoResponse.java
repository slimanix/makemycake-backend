package com.bootcamp.makemycake.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatisserieInfoResponse {
    private Long id;
    private String shopName;
    private String phoneNumber;
    private String location;
    private String profilePicture;
    private String siretNumber;
    private boolean validated;
    private boolean isValid;
} 