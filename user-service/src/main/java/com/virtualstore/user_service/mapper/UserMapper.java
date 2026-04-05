package com.virtualstore.user_service.mapper;

import com.virtualstore.user_service.dto.response.UserResponse;
import com.virtualstore.user_service.dto.request.UserRequest;
import com.virtualstore.user_service.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    @Autowired
    private PasswordEncoder encoder;

    public User toEntity(UserRequest req) {
        return User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .passwordHash(encoder.encode(req.getPassword())) 
                .role(req.getRole())
                .build();
    }

    public UserResponse toResponseDTO(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}