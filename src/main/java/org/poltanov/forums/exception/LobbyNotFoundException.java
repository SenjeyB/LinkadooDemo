package org.poltanov.forums.exception;

/**
 * Исключение, выбрасываемое при попытке доступа к несуществующему лобби.
 */
public class LobbyNotFoundException extends RuntimeException {

    /**
     * Конструктор для создания экземпляра {@link LobbyNotFoundException} с указанным сообщением.
     *
     * @param message Сообщение об ошибке.
     */
    public LobbyNotFoundException(String message) {
        super(message);
    }
}
