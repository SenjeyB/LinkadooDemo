package org.poltanov.forums.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Модель класса для представления сообщения в приложении.
 * Содержит информацию о сообщении, включая его уникальный идентификатор, текст, отправителя, лобби и время отправки.
 */
@Setter
@Getter
@Table("messages")
public class Message {

    /**
     * Уникальный идентификатор сообщения.
     */
    @Id
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
     * Идентификатор лобби, в котором отправлено сообщение.
     */
    private Long lobbyId;

    /**
     * Время отправки сообщения.
     */
    private LocalDateTime timestamp;

    /**
     * Конструктор по умолчанию.
     * Необходим для работы ORM и сериализации/десериализации.
     */
    public Message() {}
}
