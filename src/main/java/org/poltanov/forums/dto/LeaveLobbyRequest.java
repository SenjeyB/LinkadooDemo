package org.poltanov.forums.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO класс для запроса выхода из лобби.
 * Содержит идентификатор лобби, из которого пользователь хочет выйти.
 */
@Setter
@Getter
public class LeaveLobbyRequest {

    /**
     * Идентификатор лобби, из которого пользователь выходит.
     */
    private Long lobbyId;

}
