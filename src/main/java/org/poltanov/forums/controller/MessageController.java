package org.poltanov.forums.controller;

import org.poltanov.forums.dto.ChatMessage;
import org.poltanov.forums.model.Message;
import org.poltanov.forums.model.User;
import org.poltanov.forums.repository.UserRepository;
import org.poltanov.forums.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для управления сообщениями в приложении.
 * Обрабатывает отправку сообщений и получение сообщений по лобби.
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    /**
     * Конструктор для создания экземпляра MessageController.
     *
     * @param messageService сервис для обработки сообщений
     * @param userRepository репозиторий для доступа к данным пользователей
     */
    @Autowired
    public MessageController(MessageService messageService, UserRepository userRepository) {
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    /**
     * Отправка сообщения в лобби.
     *
     * @param text     Текст сообщения.
     * @param senderId ID отправителя.
     * @param lobbyId  ID лобби, в которое отправляется сообщение.
     * @return {@link ResponseEntity} с сохранённым сообщением или ошибкой при отправке.
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestParam String text, @RequestParam Long senderId, @RequestParam Long lobbyId) {
        try {
            Message message = messageService.sendMessage(text, senderId, lobbyId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при отправке сообщения");
        }
    }

    /**
     * Получение списка сообщений по ID лобби.
     *
     * @param lobbyId ID лобби, для которого необходимо получить сообщения.
     * @return {@link ResponseEntity} со списком сообщений или ошибкой при получении.
     */
    @GetMapping("/lobby/{lobbyId}")
    public ResponseEntity<?> getMessagesByLobby(@PathVariable Long lobbyId) {
        try {
            List<Message> messages = messageService.getMessagesByLobby(lobbyId);

            List<ChatMessage> chatMessages = messages.stream().map(message -> {
                Optional<User> userOpt = userRepository.findById(message.getSenderId());
                String senderNickname = userOpt.map(User::getNickname).orElse("Unknown");
                return new ChatMessage(
                        message.getId(),
                        message.getText(),
                        message.getSenderId(),
                        senderNickname,
                        message.getLobbyId(),
                        message.getTimestamp()
                );
            }).collect(Collectors.toList());

            return ResponseEntity.ok(chatMessages);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка при получении сообщений.");
        }
    }
}
