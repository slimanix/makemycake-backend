package com.bootcamp.makemycake.dto;

import com.bootcamp.makemycake.entities.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;
    private boolean enabled;
    
    // Role-specific information
    private ClientInfoResponse clientInfo;
    private PatisserieInfoResponse patisserieInfo;
} 