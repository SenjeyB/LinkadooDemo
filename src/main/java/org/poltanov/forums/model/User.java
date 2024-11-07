package org.poltanov.forums.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Модель класса для представления пользователя в приложении.
 * Содержит информацию о пользователе, включая его уникальный идентификатор, имя пользователя, пароль и никнейм.
 */
@Setter
@Getter
@Table("users")
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    private Long id;

    /**
     * Имя пользователя для аутентификации.
     */
    private String username;

    /**
     * Пароль пользователя для аутентификации.
     */
    private String password;

    /**
     * Никнейм пользователя, отображаемый в приложении.
     */
    private String nickname;
}
