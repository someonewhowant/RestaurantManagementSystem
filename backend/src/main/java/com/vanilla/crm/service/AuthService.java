package com.vanilla.crm.service;

import com.vanilla.crm.dto.auth.AuthResponse;
import com.vanilla.crm.dto.auth.LoginRequest;
import com.vanilla.crm.dto.auth.RegisterRequest;
import com.vanilla.crm.dto.auth.UserDto;
import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserDto getCurrentUser(UUID userId);
}
