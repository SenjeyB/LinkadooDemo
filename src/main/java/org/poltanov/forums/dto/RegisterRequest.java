package org.poltanov.forums.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO класс для регистрации нового пользователя.
 * Содержит имя пользователя, пароль и никнейм.
 */
@Setter
@Getter
public class RegisterRequest {

    /**
     * Имя пользователя для регистрации.
     */
    private String username;

    /**
     * Пароль пользователя для регистрации.
     */
    private String password;

    /**
     * Никнейм пользователя.
     */
    private String nickname;
}
