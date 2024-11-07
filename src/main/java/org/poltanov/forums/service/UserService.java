package org.poltanov.forums.service;

import org.poltanov.forums.model.User;
import org.poltanov.forums.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Сервис для управления пользователями.
 * Обеспечивает операции по изменению данных пользователя, такие как смена никнейма.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    /**
     * Конструктор для создания экземпляра {@link UserService}.
     *
     * @param userRepository репозиторий для управления пользователями
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Изменяет никнейм пользователя.
     *
     * @param userId      ID пользователя, чей никнейм будет изменён
     * @param newNickname Новый никнейм пользователя
     * @return {@code true}, если никнейм успешно изменён, {@code false} если пользователь не найден
     */
    public boolean changeNickname(Long userId, String newNickname) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setNickname(newNickname);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
