package org.poltanov.forums.repository;

import org.poltanov.forums.model.Lobby;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для управления сущностями {@link Lobby}.
 * Предоставляет CRUD операции для объектов {@link Lobby} с использованием {@link CrudRepository}.
 *
 * @see CrudRepository
 * @see Lobby
 */
@Repository
public interface LobbyRepository extends CrudRepository<Lobby, Long> {
}
