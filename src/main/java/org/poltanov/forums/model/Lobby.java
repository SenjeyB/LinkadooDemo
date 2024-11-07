package org.poltanov.forums.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Модель класса для представления лобби в приложении.
 * Содержит информацию о лобби, включая его уникальный идентификатор, название и идентификатор создателя.
 */
@Setter
@Getter
@Table("lobbies")
public class Lobby {

    /**
     * Уникальный идентификатор лобби.
     */
    @Id
    private Long id;

    /**
     * Название лобби.
     */
    private String name;

    /**
     * Идентификатор пользователя, создавшего лобби.
     */
    private Long creatorId;

    /**
     * Конструктор по умолчанию.
     * Необходим для работы ORM и сериализации/десериализации.
     */
    public Lobby() {}

}
