package org.poltanov.forums.controller;

import org.poltanov.forums.dto.AuthRequest;
import org.poltanov.forums.dto.RegisterRequest;
import org.poltanov.forums.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для обработки аутентификационных запросов, таких как регистрация и логин пользователей.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Конструктор для создания экземпляра AuthController.
     *
     * @param authService сервис для обработки аутентификационных операций
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Обрабатывает запрос на регистрацию нового пользователя.
     *
     * @param registerRequest объект, содержащий данные для регистрации пользователя
     * @return {@link ResponseEntity} с результатом регистрации
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    /**
     * Обрабатывает запрос на логин пользователя.
     *
     * @param authRequest объект, содержащий данные для аутентификации пользователя
     * @return {@link ResponseEntity} с JWT-токеном при успешной аутентификации
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        String jwt = authService.login(authRequest.getUsername(), authRequest.getPassword());
        Map<String, String> response = new HashMap<>();
        response.put("jwtToken", jwt);
        return ResponseEntity.ok(response);
    }
}
