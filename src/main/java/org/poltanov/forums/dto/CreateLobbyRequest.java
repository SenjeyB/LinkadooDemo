package org.poltanov.forums.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO класс для создания нового лобби.
 * Содержит название лобби.
 */
@Setter
@Getter
public class CreateLobbyRequest {

    /**
     * Название создаваемого лобби.
     */
    private String name;
}
