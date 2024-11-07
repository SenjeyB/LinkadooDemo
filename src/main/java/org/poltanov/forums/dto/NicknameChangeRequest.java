package org.poltanov.forums.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO класс для запроса изменения никнейма пользователя.
 * Содержит идентификатор пользователя и новый никнейм.
 */
@Setter
@Getter
public class NicknameChangeRequest {

    /**
     * Идентификатор пользователя, чей никнейм будет изменён.
     */
    private Long userId;

    /**
     * Новый никнейм пользователя.
     */
    private String newNickname;

}
