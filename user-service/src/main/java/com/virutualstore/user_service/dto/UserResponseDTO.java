package com.virutualstore.user_service.dto;

import com.virutualstore.user_service.entity.UserRole;
import com.virutualstore.user_service.entity.UserStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private UUID userId;
    private String name;
    private String email;
    private String mobile;
    private UserRole userType;
    private UserStatus status;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AddressResponseDTO> addresses;
}