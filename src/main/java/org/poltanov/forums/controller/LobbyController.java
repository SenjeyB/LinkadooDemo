package org.poltanov.forums.controller;

import org.poltanov.forums.dto.*;
import org.poltanov.forums.exception.UnauthorizedException;
import org.poltanov.forums.model.Lobby;
import org.poltanov.forums.model.User;
import org.poltanov.forums.repository.LobbyRepository;
import org.poltanov.forums.repository.UserRepository;
import org.poltanov.forums.service.LobbyService;
import org.poltanov.forums.service.UserDetailsServiceImpl;
import org.poltanov.forums.exception.LobbyNotFoundException;
import org.poltanov.forums.exception.NotInLobbyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для управления лобби в приложении.
 * Обрабатывает создание, присоединение, покидание, удаление лобби и получение информации о лобби и его участниках.
 */
@RestController
@RequestMapping("/lobby")
public class LobbyController {

    private final LobbyService lobbyService;
    private final UserDetailsServiceImpl userDetailsService;
    private final SimpMessagingTemplate messagingTemplate;
    private final LobbyRepository lobbyRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(LobbyController.class);

    /**
     * Конструктор для создания экземпляра LobbyController.
     *
     * @param lobbyService       сервис для управления лобби
     * @param messagingTemplate  шаблон для отправки сообщений через WebSocket
     * @param userDetailsService сервис для получения деталей пользователя
     * @param lobbyRepository    репозиторий для доступа к данным лобби
     * @param userRepository     репозиторий для доступа к данным пользователей
     */
    @Autowired
    public LobbyController(LobbyService lobbyService, SimpMessagingTemplate messagingTemplate, UserDetailsServiceImpl userDetailsService, LobbyRepository lobbyRepository, UserRepository userRepository) {
        this.lobbyService = lobbyService;
        this.messagingTemplate = messagingTemplate;
        this.userDetailsService = userDetailsService;
        this.lobbyRepository = lobbyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создание нового лобби.
     *
     * @param request        Тело запроса с названием лобби.
     * @param authentication Объект аутентификации текущего пользователя.
     * @return Ответ с информацией о созданном лобби или ошибкой.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createLobby(@RequestBody CreateLobbyRequest request, Authentication authentication) {
        String name = request.getName();

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Название лобби не может быть пустым.");
        }

        String username = authentication.getName();
        Optional<User> userOpt = userDetailsService.getUserByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не найден.");
        }

        Long userId = userOpt.get().getId();

        try {
            Lobby lobby = lobbyService.createLobby(name, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(lobby);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при создании лобби.");
        }
    }

    /**
     * Присоединение пользователя к лобби.
     *
     * @param lobbyId        ID лобби, к которому необходимо присоединиться.
     * @param authentication Объект аутентификации текущего пользователя.
     * @return Ответ с результатом операции или ошибкой.
     */
    @PostMapping("/{lobbyId}/join")
    public ResponseEntity<?> joinLobby(@PathVariable Long lobbyId, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userDetailsService.getUserByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не найден.");
        }

        Long userId = userOpt.get().getId();

        try {
            lobbyService.joinLobby(lobbyId, userId);
            return ResponseEntity.ok().body("Вы успешно присоединились к лобби.");
        } catch (LobbyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Лобби не найдено.");
        }
    }

    /**
     * Покидание лобби пользователем.
     *
     * @param request        Тело запроса с ID лобби.
     * @param authentication Объект аутентификации текущего пользователя.
     * @return Ответ о результате операции или ошибкой.
     */
    @PostMapping("/leave")
    public ResponseEntity<?> leaveLobby(@RequestBody LeaveLobbyRequest request, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userDetailsService.getUserByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не найден.");
        }

        Long userId = userOpt.get().getId();
        Long lobbyId = request.getLobbyId();

        if (lobbyId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("lobbyId не может быть null.");
        }

        try {
            lobbyService.leaveLobby(lobbyId, userId);
            return ResponseEntity.ok().body("Вы покинули лобби.");
        } catch (LobbyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Лобби не найдено.");
        } catch (NotInLobbyException e) {
            return ResponseEntity.ok().body("Вы уже не состоите в этом лобби.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при покидании лобби.");
        }
    }

    /**
     * Получение списка всех лобби.
     *
     * @return Список лобби.
     */
    @GetMapping("/list")
    public ResponseEntity<List<Lobby>> getLobbyList() {
        List<Lobby> lobbies = lobbyService.getAllLobbies();
        return ResponseEntity.ok(lobbies);
    }

    /**
     * Получение списка пользователей в конкретном лобби.
     *
     * @param lobbyId ID лобби.
     * @return Список пользователей или ошибка, если лобби не найдено.
     */
    @GetMapping("/{lobbyId}/users")
    public ResponseEntity<?> getUsersInLobby(@PathVariable Long lobbyId) {
        try {
            List<User> users = lobbyService.getUsersInLobby(lobbyId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при получении пользователей лобби.");
        }
    }

    /**
     * Удаление лобби.
     *
     * @param lobbyId        ID лобби для удаления.
     * @param authentication Объект аутентификации текущего пользователя.
     * @return Ответ о результате операции.
     */
    @PostMapping("/{lobbyId}/delete")
    public ResponseEntity<?> deleteLobby(@PathVariable Long lobbyId, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userDetailsService.getUserByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не найден.");
        }

        Long userId = userOpt.get().getId();

        try {
            lobbyService.deleteLobby(lobbyId, userId);
            return ResponseEntity.ok().body("Лобби успешно удалено.");
        } catch (LobbyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Лобби не найдено.");
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("У вас нет прав для удаления этого лобби.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при удалении лобби.");
        }
    }

    /**
     * Получение списка участников лобби.
     *
     * @param lobbyId        ID лобби.
     * @param authentication Объект аутентификации.
     * @return Список участников с их данными.
     */
    @GetMapping("/{lobbyId}/participants")
    public ResponseEntity<?> getParticipants(@PathVariable Long lobbyId, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userDetailsService.getUserByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не найден.");
        }

        List<LobbyUsers> participants = lobbyService.getParticipants(lobbyId);
        List<LobbyUsers> participantDTOs = participants.stream().map(participant -> {
            Optional<User> user = userRepository.findById(participant.getUserId());
            String nickname = user.map(User::getNickname).orElse("Unknown");
            return new LobbyUsers(participant.getUserId(), nickname);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(participantDTOs);
    }
}
