package org.poltanov.forums.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO класс для представления сообщений чата.
 * Содержит информацию о сообщении, отправителе и времени отправки.
 */
@Setter
@Getter
public class ChatMessage {

    /**
     * Уникальный идентификатор сообщения.
     */
    private Long id;

    /**
     * Текст содержания сообщения.
     */
    private String text;

    /**
     * Идентификатор пользователя, отправившего сообщение.
     */
    private Long senderId;

    /**
     * Никнейм пользователя, отправившего сообщение.
     */
    private String senderNickname;

    /**
     * Идентификатор лобби, в котором отправлено сообщение.
     */
    private Long lobbyId;

    /**
     * Время отправки сообщения.
     */
    private LocalDateTime timestamp;

    /**
     * Конструктор по умолчанию.
     */
    public ChatMessage() {}

    /**
     * Конструктор для создания экземпляра ChatMessage с указанными параметрами.
     *
     * @param id              Уникальный идентификатор сообщения.
     * @param text            Текст содержания сообщения.
     * @param senderId        Идентификатор пользователя, отправившего сообщение.
     * @param senderNickname  Никнейм пользователя, отправившего сообщение.
     * @param lobbyId         Идентификатор лобби, в котором отправлено сообщение.
     * @param timestamp       Время отправки сообщения.
     */
    public ChatMessage(Long id, String text, Long senderId, String senderNickname, Long lobbyId, LocalDateTime timestamp) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.lobbyId = lobbyId;
        this.timestamp = timestamp;
    }
}
