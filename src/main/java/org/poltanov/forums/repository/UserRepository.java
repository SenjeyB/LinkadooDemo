package org.poltanov.forums.repository;

import org.poltanov.forums.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для управления сущностями {@link User}.
 * Предоставляет операции поиска и проверки существования пользователей по имени пользователя.
 *
 * @see CrudRepository
 * @see User
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username имя пользователя для поиска
     * @return {@link Optional} пользователя, если он найден, иначе пустой
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверяет, существует ли пользователь с указанным именем пользователя.
     *
     * @param username имя пользователя для проверки
     * @return {@code true}, если пользователь существует, иначе {@code false}
     */
    boolean existsByUsername(String username);
}
