package org.poltanov.forums.service;

import org.poltanov.forums.dto.LobbyCreatedMessage;
import org.poltanov.forums.dto.LobbyDeletedMessage;
import org.poltanov.forums.dto.LobbyUsers;
import org.poltanov.forums.dto.UserJoinedMessage;
import org.poltanov.forums.dto.UserLeftMessage;
import org.poltanov.forums.exception.LobbyNotFoundException;
import org.poltanov.forums.exception.NotInLobbyException;
import org.poltanov.forums.exception.UnauthorizedException;
import org.poltanov.forums.model.Lobby;
import org.poltanov.forums.model.User;
import org.poltanov.forums.repository.LobbyRepository;
import org.poltanov.forums.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления лобби в приложении.
 * Обрабатывает создание, удаление, присоединение и покидание лобби, а также получение информации о лобби и его участниках.
 */
@Service
public class LobbyService {

    private final LobbyRepository lobbyRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(LobbyService.class);

    /**
     * Конструктор для создания экземпляра {@link LobbyService}.
     *
     * @param lobbyRepository   репозиторий для управления лобби
     * @param userRepository    репозиторий для управления пользователями
     * @param jdbcTemplate      шаблон JDBC для выполнения SQL-запросов
     * @param messagingTemplate шаблон для отправки сообщений через WebSocket
     */
    @Autowired
    public LobbyService(LobbyRepository lobbyRepository, UserRepository userRepository, JdbcTemplate jdbcTemplate, SimpMessagingTemplate messagingTemplate) {
        this.lobbyRepository = lobbyRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Создание нового лобби.
     *
     * @param name      Название лобби.
     * @param creatorId ID создателя.
     * @return Созданное лобби.
     */
    @Transactional
    public Lobby createLobby(String name, Long creatorId) {
        Lobby lobby = new Lobby();
        lobby.setName(name);
        lobby.setCreatorId(creatorId);
        lobbyRepository.save(lobby);

        Long lobbyId = lobby.getId();

        String usersTableName = getUsersTableName(lobbyId);
        String messagesTableName = getMessagesTableName(lobbyId);

        String createUsersTableSql = "CREATE TABLE IF NOT EXISTS " + usersTableName + " (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "lobby_id BIGINT NOT NULL REFERENCES lobbies(id) ON DELETE CASCADE, " +
                "user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE" +
                ")";

        String createMessagesTableSql = "CREATE TABLE IF NOT EXISTS " + messagesTableName + " (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "text TEXT NOT NULL, " +
                "sender_id BIGINT NOT NULL REFERENCES users(id), " +
                "timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")";

        jdbcTemplate.execute(createUsersTableSql);
        jdbcTemplate.execute(createMessagesTableSql);

        String insertCreatorSql = "INSERT INTO " + usersTableName + " (lobby_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(insertCreatorSql, lobbyId, creatorId);

        LobbyCreatedMessage lobbyCreatedMessage = new LobbyCreatedMessage(lobbyId, lobby.getName(), creatorId);
        messagingTemplate.convertAndSend("/topic/lobbies", lobbyCreatedMessage);

        logger.info("Лобби создано: {}", lobby);

        return lobby;
    }

    /**
     * Удаление лобби.
     *
     * @param lobbyId     ID лобби для удаления.
     * @param requesterId ID пользователя, запрашивающего удаление.
     * @throws LobbyNotFoundException Если лобби не найдено.
     * @throws UnauthorizedException  Если пользователь не является создателем.
     */
    @Transactional
    public void deleteLobby(Long lobbyId, Long requesterId) throws LobbyNotFoundException, UnauthorizedException {
        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new LobbyNotFoundException("Лобби не найдено."));

        if (!lobby.getCreatorId().equals(requesterId)) {
            throw new UnauthorizedException("Только создатель может удалить лобби.");
        }

        lobbyRepository.delete(lobby);

        String usersTableName = getUsersTableName(lobbyId);
        String messagesTableName = getMessagesTableName(lobbyId);

        String dropUsersTableSql = "DROP TABLE IF EXISTS " + usersTableName;
        String dropMessagesTableSql = "DROP TABLE IF EXISTS " + messagesTableName;

        jdbcTemplate.execute(dropUsersTableSql);
        jdbcTemplate.execute(dropMessagesTableSql);

        LobbyDeletedMessage lobbyDeletedMessage = new LobbyDeletedMessage(lobbyId, lobby.getName());
        messagingTemplate.convertAndSend("/topic/lobbies", lobbyDeletedMessage);

        logger.info("Лобби удалено: {}", lobby);
    }

    /**
     * Присоединение пользователя к лобби.
     *
     * @param lobbyId ID лобби.
     * @param userId  ID пользователя.
     * @throws LobbyNotFoundException Если лобби не найдено.
     */
    @Transactional
    public void joinLobby(Long lobbyId, Long userId) throws LobbyNotFoundException {
        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new LobbyNotFoundException("Лобби не найдено."));

        String usersTableName = getUsersTableName(lobbyId);

        String checkUserSql = "SELECT COUNT(*) FROM " + usersTableName + " WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
        if (count != null && count > 0) {
            return;
        }

        String insertUserSql = "INSERT INTO " + usersTableName + " (lobby_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(insertUserSql, lobbyId, userId);

        Optional<User> userOpt = userRepository.findById(userId);
        userOpt.ifPresent(user -> {
            UserJoinedMessage userJoinedMessage = new UserJoinedMessage(userId, user.getNickname());
            messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/participants", userJoinedMessage);
        });

        logger.info("Пользователь id={} присоединился к лобби id={}", userId, lobbyId);
    }

    /**
     * Покинуть лобби.
     *
     * @param lobbyId ID лобби.
     * @param userId  ID пользователя.
     * @throws LobbyNotFoundException Если лобби не найдено.
     * @throws NotInLobbyException    Если пользователь не в лобби.
     */
    @Transactional
    public void leaveLobby(Long lobbyId, Long userId) throws LobbyNotFoundException, NotInLobbyException {
        if (lobbyId == null) {
            throw new IllegalArgumentException("lobbyId не может быть null.");
        }

        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new LobbyNotFoundException("Лобби не найдено."));

        String usersTableName = getUsersTableName(lobbyId);

        String checkUserSql = "SELECT COUNT(*) FROM " + usersTableName + " WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
        if (count == null || count == 0) {
            logger.warn("Пользователь id={} не состоит в лобби id={}", userId, lobbyId);
            return;
        }

        String deleteUserSql = "DELETE FROM " + usersTableName + " WHERE user_id = ?";
        jdbcTemplate.update(deleteUserSql, userId);

        Optional<User> userOpt = userRepository.findById(userId);
        userOpt.ifPresent(user -> {
            UserLeftMessage userLeftMessage = new UserLeftMessage(userId, user.getNickname());
            messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/participants", userLeftMessage);
        });

        logger.info("Пользователь id={} покинул лобби id={}", userId, lobbyId);
    }

    /**
     * Получение списка всех лобби.
     *
     * @return Список лобби.
     */
    public List<Lobby> getAllLobbies() {
        List<Lobby> lobbies = new ArrayList<>();
        lobbyRepository.findAll().forEach(lobbies::add);
        return lobbies;
    }

    /**
     * Получение списка пользователей в лобби.
     *
     * @param lobbyId ID лобби.
     * @return Список пользователей.
     * @throws LobbyNotFoundException Если лобби не найдено.
     */
    public List<User> getUsersInLobby(Long lobbyId) throws LobbyNotFoundException {
        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new LobbyNotFoundException("Лобби не найдено."));

        String usersTableName = getUsersTableName(lobbyId);

        String querySql = "SELECT u.id, u.username, u.password, u.nickname " +
                "FROM " + usersTableName + " lu " +
                "JOIN users u ON lu.user_id = u.id";

        List<User> users = jdbcTemplate.query(querySql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setNickname(rs.getString("nickname"));
            return user;
        });

        return users;
    }

    /**
     * Получение списка участников лобби (DTO).
     *
     * @param lobbyId ID лобби.
     * @return Список участников.
     * @throws LobbyNotFoundException Если лобби не найдено.
     */
    public List<LobbyUsers> getParticipants(Long lobbyId) throws LobbyNotFoundException {
        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new LobbyNotFoundException("Лобби не найдено."));

        String usersTableName = getUsersTableName(lobbyId);

        String querySql = "SELECT u.id, u.nickname " +
                "FROM " + usersTableName + " lu " +
                "JOIN users u ON lu.user_id = u.id";

        List<LobbyUsers> participants = jdbcTemplate.query(querySql, (rs, rowNum) -> {
            LobbyUsers lobbyUser = new LobbyUsers();
            lobbyUser.setUserId(rs.getLong("id"));
            lobbyUser.setNickname(rs.getString("nickname"));
            return lobbyUser;
        });

        return participants;
    }

    /**
     * Генерация имени таблицы пользователей для конкретного лобби.
     *
     * @param lobbyId ID лобби.
     * @return Имя таблицы пользователей.
     */
    private String getUsersTableName(Long lobbyId) {
        return "lobby_" + lobbyId + "_users";
    }

    /**
     * Генерация имени таблицы сообщений для конкретного лобби.
     *
     * @param lobbyId ID лобби.
     * @return Имя таблицы сообщений.
     */
    private String getMessagesTableName(Long lobbyId) {
        return "lobby_" + lobbyId + "_messages";
    }
}
