package com.virutualstore.user_service.mapper;

import com.virutualstore.user_service.dto.UserRequestDTO;
import com.virutualstore.user_service.dto.UserResponseDTO;
import com.virutualstore.user_service.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UserMapper {

    public User toEntity(UserRequestDTO dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .mobile(dto.getMobile())
                .password(dto.getPassword()) 
                .userType(dto.getUserType())
                .build();
    }

    public UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .userType(user.getUserType())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .addresses(new ArrayList<>()) // populate when Address mapper is ready
                .build();
    }
}