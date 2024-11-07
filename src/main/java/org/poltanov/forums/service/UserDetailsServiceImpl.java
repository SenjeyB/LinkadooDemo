package org.poltanov.forums.service;

import org.poltanov.forums.model.User;
import org.poltanov.forums.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Сервис для загрузки деталей пользователя при аутентификации.
 * Реализует интерфейс {@link UserDetailsService} для интеграции с Spring Security.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Конструктор для создания экземпляра {@link UserDetailsServiceImpl}.
     *
     * @param userRepository репозиторий для управления пользователями
     */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Загружает пользователя по имени пользователя.
     *
     * @param username имя пользователя, по которому необходимо загрузить детали
     * @return {@link UserDetails} содержащий информацию о пользователе
     * @throws UsernameNotFoundException если пользователь с указанным именем не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }

    /**
     * Получает пользователя по имени пользователя.
     *
     * @param username имя пользователя для поиска
     * @return {@link Optional} пользователя, если он найден, иначе пустой {@link Optional}
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
