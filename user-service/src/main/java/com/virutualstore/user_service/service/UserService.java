package com.virutualstore.user_service.service;

import com.virutualstore.user_service.dto.UserRequestDTO;
import com.virutualstore.user_service.dto.UserResponseDTO;
import com.virutualstore.user_service.dto.UserUpdateDTO;
import com.virutualstore.user_service.entity.User;
import com.virutualstore.user_service.entity.UserRole;
import com.virutualstore.user_service.entity.UserStatus;
import com.virutualstore.user_service.exception.ResourceAlreadyExistsException;
import com.virutualstore.user_service.exception.ResourceNotFoundException;
import com.virutualstore.user_service.mapper.UserMapper;
import com.virutualstore.user_service.repository.UserRepository;
import com.virutualstore.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        log.info("Creating user with email: {}", requestDTO.getEmail());

        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("User already exists with email: " + requestDTO.getEmail());
        }
        if (userRepository.existsByMobile(requestDTO.getMobile())) {
            throw new ResourceAlreadyExistsException("User already exists with mobile: " + requestDTO.getMobile());
        }

        User user = userMapper.toEntity(requestDTO);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getUserId());
        return userMapper.toResponseDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID userId) {
        User user = findUserById(userId);
        return userMapper.toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(UserRole role) {
        return userRepository.findByUserType(role)
                .stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status)
                .stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO updateUser(UUID userId, UserUpdateDTO updateDTO) {
        log.info("Updating user with id: {}", userId);
        User user = findUserById(userId);

        if (updateDTO.getName() != null) {
            user.setName(updateDTO.getName());
        }
        
        if (updateDTO.getMobile() != null) {
            if (userRepository.existsByMobile(updateDTO.getMobile())) {
                throw new ResourceAlreadyExistsException("Mobile already in use: " + updateDTO.getMobile());
            }
            user.setMobile(updateDTO.getMobile());
        }

        if (updateDTO.getPassword() != null) {
            user.setPassword(updateDTO.getPassword());
        }

        return userMapper.toResponseDTO(userRepository.save(user));
    }

    public void deleteUser(UUID userId) {
        log.info("Deleting user with id: {}", userId);
        User user = findUserById(userId);
        userRepository.delete(user);
    }

    public UserResponseDTO activateUser(UUID userId) {
        User user = findUserById(userId);
        user.activate();
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    public UserResponseDTO deactivateUser(UUID userId) {
        User user = findUserById(userId);
        user.deactivate();
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    public UserResponseDTO verifyEmail(UUID userId) {
        User user = findUserById(userId);
        user.verifyEmail();
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    public UserResponseDTO verifyPhone(UUID userId) {
        User user = findUserById(userId);
        user.verifyPhone();
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    public void updateLastLogin(UUID userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}