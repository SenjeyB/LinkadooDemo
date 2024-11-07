package org.poltanov.forums.exception;

/**
 * Исключение, выбрасываемое при попытке пользователя покинуть лобби, в котором он не состоит.
 */
public class NotInLobbyException extends RuntimeException {

    /**
     * Конструктор для создания экземпляра {@link NotInLobbyException} с указанным сообщением.
     *
     * @param message Сообщение об ошибке.
     */
    public NotInLobbyException(String message) {
        super(message);
    }
}
