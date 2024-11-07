package org.poltanov.forums.controller;

import org.poltanov.forums.dto.NicknameChangeRequest;
import org.poltanov.forums.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления операциями, связанными с пользователями.
 * Обрабатывает запросы на изменение никнейма пользователя.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /**
     * Конструктор для создания экземпляра UserController.
     *
     * @param userService сервис для управления пользователями
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Изменение никнейма пользователя.
     *
     * @param request объект {@link NicknameChangeRequest}, содержащий ID пользователя и новый никнейм
     * @return {@link ResponseEntity} с сообщением об успешном обновлении или ошибкой при обновлении
     */
    @PutMapping("/change-nickname")
    public ResponseEntity<?> changeNickname(@RequestBody NicknameChangeRequest request) {
        boolean success = userService.changeNickname(request.getUserId(), request.getNewNickname());
        if (success) {
            return ResponseEntity.ok("Никнейм успешно обновлён");
        } else {
            return ResponseEntity.badRequest().body("Ошибка во время обновления никнейма");
        }
    }
}
