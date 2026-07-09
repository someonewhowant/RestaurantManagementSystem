package com.vanilla.crm.service.impl;

import com.vanilla.crm.exception.ResourceNotFoundException;
import com.vanilla.crm.exception.DuplicateResourceException;

import com.vanilla.crm.repository.UserRepository;

import com.vanilla.crm.dto.auth.AuthResponse;
import com.vanilla.crm.dto.auth.LoginRequest;
import com.vanilla.crm.dto.auth.RegisterRequest;
import com.vanilla.crm.dto.auth.UserDto;
import com.vanilla.crm.entity.User;
import com.vanilla.crm.mapper.AuthMapper;
import com.vanilla.crm.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vanilla.crm.service.AuthService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthMapper authMapper;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .restaurantName(request.getRestaurantName())
                .role(User.Role.OWNER) // Default role for new registration
                .build();

        userRepository.save(user);

        // Auto-login after registration
        return login(new LoginRequest(request.getEmail(), request.getPassword()));
    }
    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new AuthResponse(jwt, authMapper.toDto(user));
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return authMapper.toDto(user);
    }
}
