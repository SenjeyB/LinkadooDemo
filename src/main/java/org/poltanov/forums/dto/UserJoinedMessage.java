package org.poltanov.forums.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO класс для уведомления о присоединении участника к лобби.
 * Содержит информацию о пользователе, который присоединился, включая его ID и никнейм.
 */
@Setter
@Getter
public class UserJoinedMessage {

    /**
     * Тип сообщения. По умолчанию устанавливается значение "PARTICIPANT_JOINED".
     */
    private String type = "PARTICIPANT_JOINED";

    /**
     * Уникальный идентификатор пользователя, который присоединился к лобби.
     */
    private Long userId;

    /**
     * Никнейм пользователя, который присоединился к лобби.
     */
    private String nickname;

    /**
     * Конструктор для создания экземпляра UserJoinedMessage с указанными параметрами.
     *
     * @param userId    Уникальный идентификатор пользователя.
     * @param nickname  Никнейм пользователя.
     */
    public UserJoinedMessage(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }

}
