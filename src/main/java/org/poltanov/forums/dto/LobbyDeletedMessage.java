package org.poltanov.forums.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO класс для уведомления об удалении лобби.
 * Содержит информацию о удалённом лобби, включая его ID и название.
 */
@Setter
@Getter
public class LobbyDeletedMessage {

    /**
     * Тип сообщения. По умолчанию устанавливается значение "LOBBY_DELETED".
     */
    private String type = "LOBBY_DELETED";

    /**
     * Уникальный идентификатор удалённого лобби.
     */
    private Long id;

    /**
     * Название удалённого лобби.
     */
    private String name;

    /**
     * Конструктор для создания экземпляра LobbyDeletedMessage с указанными параметрами.
     *
     * @param id   Уникальный идентификатор удалённого лобби.
     * @param name Название удалённого лобби.
     */
    public LobbyDeletedMessage(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
