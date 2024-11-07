package org.poltanov.forums.exception;

/**
 * Исключение, выбрасываемое при попытке доступа пользователя к ресурсу без необходимых прав.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Конструктор для создания экземпляра {@link UnauthorizedException} с указанным сообщением.
     *
     * @param message Сообщение об ошибке.
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
