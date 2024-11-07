package org.poltanov.forums.service;

import org.poltanov.forums.dto.RegisterRequest;
import org.poltanov.forums.model.User;
import org.poltanov.forums.repository.UserRepository;
import org.poltanov.forums.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Сервис для управления аутентификацией пользователей.
 * Обрабатывает регистрацию и вход пользователей, генерируя JWT токены для аутентифицированных сессий.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Конструктор для создания экземпляра {@link AuthService}.
     *
     * @param userRepository        репозиторий для управления пользователями
     * @param passwordEncoder       энкодер паролей для безопасного хранения
     * @param jwtUtil               утилита для работы с JWT токенами
     * @param authenticationManager менеджер аутентификации для проверки учетных данных
     * @param userDetailsService    сервис для загрузки деталей пользователя
     */
    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param registerRequest объект {@link RegisterRequest}, содержащий данные для регистрации
     * @return {@link ResponseEntity} с сообщением об успешной регистрации или ошибкой
     */
    public ResponseEntity<?> register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Имя пользователя уже занято");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setNickname(registerRequest.getNickname());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Регистрация успешна");
    }

    /**
     * Аутентифицирует пользователя и генерирует JWT токен при успешной аутентификации.
     *
     * @param username имя пользователя
     * @param password пароль пользователя
     * @return JWT токен в виде строки
     * @throws RuntimeException если аутентификация не удалась
     */
    public String login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Неверное имя пользователя или пароль", e);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Long userId = user.getId();
        String nickname = user.getNickname();

        return jwtUtil.generateToken(userDetails, userId, nickname);
    }
}
