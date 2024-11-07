package org.poltanov.forums.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO класс для аутентификационных запросов.
 * Содержит имя пользователя и пароль.
 */
@Setter
@Getter
public class AuthRequest {

    /**
     * Имя пользователя для аутентификации.
     */
    private String username;

    /**
     * Пароль пользователя для аутентификации.
     */
    private String password;

}
