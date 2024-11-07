package org.poltanov.forums.config;

import org.poltanov.forums.service.UserDetailsServiceImpl;
import org.poltanov.forums.util.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурация безопасности для веб-приложения.
 * Настраивает аутентификацию, авторизацию и фильтры безопасности.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;

    /**
     * Конструктор для создания экземпляра WebSecurityConfig.
     *
     * @param userDetailsService реализация сервиса для загрузки деталей пользователя
     * @param jwtRequestFilter   фильтр для обработки JWT-токенов в запросах
     */
    @Autowired
    public WebSecurityConfig(UserDetailsServiceImpl userDetailsService, JwtRequestFilter jwtRequestFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    /**
     * Создаёт и возвращает {@link PasswordEncoder} для шифрования паролей.
     *
     * @return экземпляр {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Создаёт и возвращает {@link DaoAuthenticationProvider} для аутентификации пользователей.
     * Устанавливает сервис для загрузки деталей пользователя и {@link PasswordEncoder}.
     *
     * @return настроенный {@link DaoAuthenticationProvider}
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Устанавливаем ваш UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder()); // Устанавливаем ваш PasswordEncoder
        return authProvider;
    }

    /**
     * Конфигурирует и возвращает {@link AuthenticationManager} для управления аутентификацией.
     *
     * @param authConfig конфигурация аутентификации
     * @return настроенный {@link AuthenticationManager}
     * @throws Exception если возникает ошибка при настройке
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Конфигурирует {@link SecurityFilterChain} для управления доступом к ресурсам и фильтрами безопасности.
     *
     * @param http объект {@link HttpSecurity} для настройки безопасности
     * @return настроенная цепочка фильтров безопасности
     * @throws Exception если возникает ошибка при настройке
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/index.html", "/login.html", "/register.html",
                                "/static/**", "/css/**", "/js/**", "/images/**",
                                "/favicon.ico", "/auth/**", "/ws/**", "/error"
                        ).permitAll()
                        .requestMatchers("/lobby/create").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
