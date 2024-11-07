package org.poltanov.forums.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO класс для представления участников лобби.
 * Содержит идентификатор пользователя и его никнейм.
 */
@Setter
@Getter
public class LobbyUsers {

    /**
     * Идентификатор пользователя.
     */
    private Long userId;

    /**
     * Никнейм пользователя.
     */
    private String nickname;

    /**
     * Конструктор по умолчанию.
     */
    public LobbyUsers() {}

    /**
     * Конструктор для создания экземпляра LobbyUsers с указанными параметрами.
     *
     * @param userId    Идентификатор пользователя.
     * @param nickname  Никнейм пользователя.
     */
    public LobbyUsers(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }
}
