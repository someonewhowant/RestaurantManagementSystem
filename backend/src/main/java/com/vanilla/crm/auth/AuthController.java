package com.vanilla.crm.auth;

import com.vanilla.crm.auth.dto.AuthResponse;
import com.vanilla.crm.auth.dto.LoginRequest;
import com.vanilla.crm.auth.dto.RegisterRequest;
import com.vanilla.crm.auth.dto.UserDto;
import com.vanilla.crm.auth.entity.User;
import com.vanilla.crm.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Регистрация, авторизация и получение текущего пользователя")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Operation(summary = "Вход в систему", description = "Авторизация по email и паролю. Возвращает JWT-токен.")
    @ApiResponse(responseCode = "200", description = "Успешная авторизация")
    @ApiResponse(responseCode = "401", description = "Неверные учётные данные")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Регистрация", description = "Создание нового аккаунта владельца ресторана. Автоматический вход после регистрации.")
    @ApiResponse(responseCode = "200", description = "Успешная регистрация")
    @ApiResponse(responseCode = "409", description = "Email уже используется")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Текущий пользователь", description = "Возвращает профиль авторизованного пользователя по JWT-токену.")
    @ApiResponse(responseCode = "200", description = "Профиль пользователя")
    @ApiResponse(responseCode = "401", description = "Не авторизован")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }
}
