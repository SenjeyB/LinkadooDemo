package org.poltanov.forums.controller;

import org.poltanov.forums.model.Message;
import org.poltanov.forums.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * Контроллер для обработки сообщений чата.
 * Обрабатывает отправку и распространение сообщений между пользователями.
 */
@Controller
public class ChatController {

    private final MessageService messageService;

    /**
     * Конструктор для создания экземпляра ChatController.
     *
     * @param messageService сервис для обработки сообщений
     */
    public ChatController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Обрабатывает отправку сообщения в чат.
     * Сохраняет сообщение и распространяет его среди участников лобби.
     *
     * @param message объект {@link Message}, содержащий информацию о сообщении
     * @throws Exception если происходит ошибка при обработке сообщения
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(Message message) throws Exception {
        Message savedMessage = messageService.sendMessage(message.getText(), message.getSenderId(), message.getLobbyId());
        messageService.broadcastMessage(savedMessage);
    }
}
