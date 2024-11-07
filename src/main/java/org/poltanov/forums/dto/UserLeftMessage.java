package org.poltanov.forums.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO класс для уведомления об уходе участника из лобби.
 * Содержит информацию о пользователе, который покинул лобби, включая его ID и никнейм.
 */
@Setter
@Getter
public class UserLeftMessage {

    /**
     * Тип сообщения. По умолчанию устанавливается значение "PARTICIPANT_LEFT".
     */
    private String type = "PARTICIPANT_LEFT";

    /**
     * Уникальный идентификатор пользователя, который покинул лобби.
     */
    private Long userId;

    /**
     * Никнейм пользователя, который покинул лобби.
     */
    private String nickname;


    /**
     * Конструктор для создания экземпляра UserLeftMessage с указанными параметрами.
     *
     * @param userId    Уникальный идентификатор пользователя.
     * @param nickname  Никнейм пользователя.
     */
    public UserLeftMessage(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }
}
