package org.poltanov.forums.service;

import org.poltanov.forums.dto.ChatMessage;
import org.poltanov.forums.model.Message;
import org.poltanov.forums.model.User;
import org.poltanov.forums.repository.UserRepository;
import org.poltanov.forums.util.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления сообщениями в приложении.
 * Обрабатывает отправку, получение и распространение сообщений внутри лобби.
 */
@Service
public class MessageService {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    /**
     * Конструктор для создания экземпляра {@link MessageService}.
     *
     * @param jdbcTemplate      шаблон JDBC для выполнения SQL-запросов
     * @param userRepository    репозиторий для управления пользователями
     * @param messagingTemplate шаблон для отправки сообщений через WebSocket
     */
    @Autowired
    public MessageService(JdbcTemplate jdbcTemplate, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Отправка сообщения.
     *
     * @param text     Текст сообщения.
     * @param senderId ID отправителя.
     * @param lobbyId  ID лобби.
     * @return Сохранённое сообщение.
     * @throws Exception Если возникает ошибка при сохранении сообщения.
     */
    @Transactional
    public Message sendMessage(String text, Long senderId, Long lobbyId) throws Exception {
        try {
            String encryptedText = EncryptionUtil.encrypt(text);

            String messagesTableName = getMessagesTableName(lobbyId);

            String insertSql = "INSERT INTO " + messagesTableName + " (text, sender_id, timestamp) VALUES (?, ?, ?) RETURNING id, text, sender_id, timestamp";
            Message savedMessage = jdbcTemplate.queryForObject(
                    insertSql,
                    new Object[]{encryptedText, senderId, LocalDateTime.now()},
                    (rs, rowNum) -> {
                        Message msg = new Message();
                        msg.setId(rs.getLong("id"));
                        msg.setText(rs.getString("text"));
                        msg.setSenderId(rs.getLong("sender_id"));
                        msg.setLobbyId(lobbyId);
                        msg.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                        return msg;
                    }
            );

            logger.info("Сообщение сохранено: {}", savedMessage.getId());

            return savedMessage;
        } catch (Exception e) {
            logger.error("Ошибка при сохранении сообщения: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Получение сообщений лобби.
     *
     * @param lobbyId ID лобби.
     * @return Список сообщений.
     * @throws Exception Если возникает ошибка при получении сообщений.
     */
    public List<Message> getMessagesByLobby(Long lobbyId) throws Exception {
        try {
            String messagesTableName = getMessagesTableName(lobbyId);

            String querySql = "SELECT id, text, sender_id, timestamp FROM " + messagesTableName + " ORDER BY timestamp ASC";

            List<Message> encryptedMessages = jdbcTemplate.query(querySql, (rs, rowNum) -> {
                Message message = new Message();
                message.setId(rs.getLong("id"));
                message.setText(rs.getString("text"));
                message.setSenderId(rs.getLong("sender_id"));
                message.setLobbyId(lobbyId);
                message.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                return message;
            });

            return encryptedMessages.stream().map(message -> {
                try {
                    String decryptedText = EncryptionUtil.decrypt(message.getText());
                    message.setText(decryptedText);
                    return message;
                } catch (Exception e) {
                    logger.error("Ошибка при дешифровке сообщения: {}", e.getMessage(), e);
                    return message;
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Ошибка при получении сообщений для лобби {}: {}", lobbyId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Бродкастинг сообщения через WebSocket.
     *
     * @param message Сообщение для распространения.
     */
    public void broadcastMessage(Message message) {
        String decryptedText;
        try {
            decryptedText = EncryptionUtil.decrypt(message.getText());
        } catch (Exception e) {
            decryptedText = "Ошибка при дешифровке сообщения.";
        }

        String senderNickname = "Unknown";
        Optional<User> userOpt = userRepository.findById(message.getSenderId());
        if (userOpt.isPresent()) {
            senderNickname = userOpt.get().getNickname();
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(message.getId());
        chatMessage.setText(decryptedText);
        chatMessage.setSenderId(message.getSenderId());
        chatMessage.setSenderNickname(senderNickname);
        chatMessage.setLobbyId(message.getLobbyId());
        chatMessage.setTimestamp(message.getTimestamp());

        messagingTemplate.convertAndSend("/topic/lobby/" + message.getLobbyId() + "/messages", chatMessage);
    }

    /**
     * Получение названия таблицы сообщений для лобби.
     *
     * @param lobbyId ID лобби.
     * @return Название таблицы сообщений.
     */
    private String getMessagesTableName(Long lobbyId) {
        return "lobby_" + lobbyId + "_messages";
    }
}
