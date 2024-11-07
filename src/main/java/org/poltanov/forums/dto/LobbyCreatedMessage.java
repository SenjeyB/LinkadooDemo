package org.poltanov.forums.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO класс для уведомления о создании нового лобби.
 * Содержит информацию о созданном лобби, включая его ID, название и ID создателя.
 */
@Setter
@Getter
public class LobbyCreatedMessage {

    /**
     * Тип сообщения. По умолчанию устанавливается значение "LOBBY_CREATED".
     */
    private String type = "LOBBY_CREATED";

    /**
     * Уникальный идентификатор созданного лобби.
     */
    private Long id;

    /**
     * Название созданного лобби.
     */
    private String name;

    /**
     * Идентификатор пользователя, создавшего лобби.
     */
    private Long creatorId;

    /**
     * Конструктор для создания экземпляра LobbyCreatedMessage с указанными параметрами.
     *
     * @param id         Уникальный идентификатор созданного лобби.
     * @param name       Название созданного лобби.
     * @param creatorId  Идентификатор пользователя, создавшего лобби.
     */
    public LobbyCreatedMessage(Long id, String name, Long creatorId) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
    }
}
