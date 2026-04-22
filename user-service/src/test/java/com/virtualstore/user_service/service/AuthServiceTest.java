package com.virtualstore.user_service.service;

import com.virtualstore.user_service.config.AppProperties;
import com.virtualstore.user_service.config.JwtProperties;
import com.virtualstore.user_service.dto.request.SignupRequest;
import com.virtualstore.user_service.dto.response.MessageResponse;
import com.virtualstore.user_service.entity.Role;
import com.virtualstore.user_service.entity.Status;
import com.virtualstore.user_service.entity.User;
import com.virtualstore.user_service.repository.InvalidatedTokenRepository;
import com.virtualstore.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtProperties jwtProperties;

    private AppProperties appProperties;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        authService = new AuthService(
                userRepository,
                invalidatedTokenRepository,
                passwordEncoder,
                jwtService,
                authenticationManager,
                emailService,
                jwtProperties,
                appProperties);
    }

    @Test
    void signupCreatesActiveUserWithoutVerification() {
        SignupRequest request = new SignupRequest();
        request.setEmail("new@virtualstore.com");
        request.setFullName("New User");
        request.setPassword("Password@123");
        request.setPhone("+919876543210");
        request.setRole(Role.CUSTOMER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = authService.signup(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("new@virtualstore.com");
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(userCaptor.getValue().isEnabled()).isTrue();
        assertThat(userCaptor.getValue().isEmailVerified()).isTrue();
        assertThat(response.getMessage()).contains("Please log in");
    }
}
