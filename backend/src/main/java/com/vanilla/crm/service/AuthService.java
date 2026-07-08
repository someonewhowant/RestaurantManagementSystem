package com.vanilla.crm.service;

import com.vanilla.crm.dto.auth.AuthResponse;
import com.vanilla.crm.dto.auth.LoginRequest;
import com.vanilla.crm.dto.auth.RegisterRequest;
import com.vanilla.crm.dto.auth.UserDto;
import com.vanilla.crm.entity.User;
import com.vanilla.crm.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserDto getCurrentUser(java.util.UUID userId);
}
