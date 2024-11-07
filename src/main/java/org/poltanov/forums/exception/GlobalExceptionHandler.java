package org.poltanov.forums.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Глобальный обработчик исключений для приложения.
 * Перехватывает и обрабатывает различные типы исключений, возвращая соответствующие HTTP-статусы и сообщения.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение {@link LobbyNotFoundException}.
     *
     * @param ex экземпляр {@link LobbyNotFoundException}, которое было выброшено
     * @return {@link ResponseEntity} с HTTP-статусом 404 (NOT_FOUND) и сообщением об ошибке
     */
    @ExceptionHandler(LobbyNotFoundException.class)
    public ResponseEntity<String> handleLobbyNotFoundException(LobbyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Обрабатывает исключение {@link NotInLobbyException}.
     *
     * @param ex экземпляр {@link NotInLobbyException}, которое было выброшено
     * @return {@link ResponseEntity} с HTTP-статусом 400 (BAD_REQUEST) и сообщением об ошибке
     */
    @ExceptionHandler(NotInLobbyException.class)
    public ResponseEntity<String> handleNotInLobbyException(NotInLobbyException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Обрабатывает исключение {@link UnauthorizedException}.
     *
     * @param ex экземпляр {@link UnauthorizedException}, которое было выброшено
     * @return {@link ResponseEntity} с HTTP-статусом 403 (FORBIDDEN) и сообщением об ошибке
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
