let stompClient = null;
const jwtToken = localStorage.getItem('jwtToken');
let senderId = null;
let senderNickname = null;
let lobbyId = null;
let stompSubscription = null;
let participantSubscription = null;
let currentLobbyCreatorId = null;

let isLobbyCreator = false;

if (!jwtToken) {
    window.location.href = '/login.html';
} else {
    try {
        const decodedToken = jwt_decode(jwtToken);
        senderId = decodedToken.userId;
        senderNickname = decodedToken.nickname;

        document.getElementById('userName').innerText = senderNickname;
    } catch (error) {
        console.error('Ошибка при декодировании JWT-токена:', error);
        alert('Неверный токен. Пожалуйста, войдите снова.');
        window.location.href = '/login.html';
    }
}

function connect() {
    const socket = new SockJS('/ws?token=' + jwtToken);
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/lobbies', function (lobbyOutput) {
            const message = JSON.parse(lobbyOutput.body);
            console.log('Получено сообщение лобби:', message);
            if (message.type === 'LOBBY_CREATED') {
                addLobbyToList(message);
            } else if (message.type === 'LOBBY_DELETED') {
                removeLobbyFromList(message.id, message.name);
                if (message.id === lobbyId) {
                    closeChat();
                }
            }
        });
    }, function (error) {
        console.error('STOMP error:', error);
    });
}

function loadLobbyList() {
    fetch('/lobby/list', {
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        }
    })
        .then(response => response.json())
        .then(data => {
            const lobbyListDiv = document.querySelector('.lobby-list');
            lobbyListDiv.innerHTML = '';
            data.forEach(function(lobby) {
                addLobbyToList(lobby);
            });
        })
        .catch(error => console.error('Ошибка при загрузке списка лобби:', error));
}

async function selectLobby(selectedLobbyId, lobbyName, lobbyCreatorId) {
    if (lobbyId) {
        try {
            await leaveLobby();
        } catch (error) {
            console.error('Не удалось покинуть текущее лобби:', error);
            return;
        }
    }

    lobbyId = selectedLobbyId;
    currentLobbyCreatorId = lobbyCreatorId;

    isLobbyCreator = (senderId === lobbyCreatorId);
    const deleteLobbyButton = document.getElementById('deleteLobbyButton');
    deleteLobbyButton.style.display = isLobbyCreator ? 'block' : 'none';

    document.querySelector('.chat-container').style.display = 'flex';

    document.getElementById('currentLobbyName').innerText = lobbyName;

    document.getElementById('messages').innerHTML = '';

    const leaveLobbyButton = document.querySelector('.chat-header .leave-lobby-button');
    leaveLobbyButton.style.display = 'block';

    joinLobby(lobbyId);

    if (stompSubscription) {
        stompSubscription.unsubscribe();
        stompSubscription = null;
    }
    if (participantSubscription) {
        participantSubscription.unsubscribe();
        participantSubscription = null;
    }

    stompSubscription = stompClient.subscribe('/topic/lobby/' + lobbyId + '/messages', function(messageOutput) {
        const message = JSON.parse(messageOutput.body);
        console.log('Получено сообщение чата:', message);
        showMessage(message);
    });

    participantSubscription = stompClient.subscribe('/topic/lobby/' + lobbyId + '/participants', function(participantOutput) {
        const participantMessage = JSON.parse(participantOutput.body);
        console.log('Получено сообщение участника:', participantMessage);
        if (participantMessage.type === 'PARTICIPANT_JOINED') {
            addParticipant(participantMessage);
        } else if (participantMessage.type === 'PARTICIPANT_LEFT') {
            removeParticipant(participantMessage.userId);
        }
    });

    try {
        const response = await fetch('/messages/lobby/' + lobbyId, {
            headers: {
                'Authorization': 'Bearer ' + jwtToken
            }
        });
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText);
        }
        const messages = await response.json();
        messages.forEach(function(message) {
            showMessage(message);
        });
        const messagesDiv = document.getElementById('messages');
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
    } catch (error) {
        console.error('Ошибка при загрузке сообщений лобби:', error);
        alert('Не удалось загрузить сообщения лобби.');
    }

    loadParticipants(lobbyId);
}

function addLobbyToList(lobby) {
    const lobbyListDiv = document.querySelector('.lobby-list');

    if (document.getElementById('lobby-' + lobby.id)) {
        console.warn('Лобби с id=' + lobby.id + ' уже существует в списке.');
        return;
    }

    const lobbyElement = document.createElement('div');
    lobbyElement.classList.add('lobby-item');
    lobbyElement.id = 'lobby-' + lobby.id;

    const lobbyName = document.createElement('span');
    lobbyName.textContent = lobby.name;

    if (lobby.creatorId === senderId) {
        const adminTag = document.createElement('span');
        adminTag.classList.add('admin-tag');
        adminTag.textContent = 'Админ';
        lobbyElement.appendChild(lobbyName);
        lobbyElement.appendChild(adminTag);
    } else {
        lobbyElement.appendChild(lobbyName);
    }

    lobbyElement.onclick = function() {
        selectLobby(lobby.id, lobby.name, lobby.creatorId)
            .catch(error => {
                console.error('Ошибка при выборе лобби:', error);
            });
    };
    lobbyListDiv.appendChild(lobbyElement);
}

function showMessage(message) {
    const messagesDiv = document.getElementById('messages');

    const isAtBottom = messagesDiv.scrollHeight - messagesDiv.scrollTop <= messagesDiv.clientHeight + 50;

    const messageDiv = document.createElement('div');

    if (message.type === 'system' || message.type === 'error') {
        return;
    } else {
        const isCurrentUser = (message.senderId === senderId);
        const isAdmin = (message.senderId === currentLobbyCreatorId);

        if (isCurrentUser) {
            messageDiv.className = 'message user current';
        } else if (isAdmin) {
            messageDiv.className = 'message user admin';
        } else {
            messageDiv.className = 'message user';
        }

        const timestamp = new Date(message.timestamp);
        const options = {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit'
        };
        const formattedTime = timestamp.toLocaleDateString('ru-RU', options);

        const senderDisplayName = message.senderNickname + (isAdmin ? ' (Админ)' : '');

        messageDiv.innerHTML = `<strong>${senderDisplayName}:</strong> ${message.text} <br><span class="message-timestamp">${formattedTime}</span>`;
    }

    requestAnimationFrame(() => {
        messageDiv.classList.add('show');
    });

    messagesDiv.appendChild(messageDiv);

    if (isAtBottom) {
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
    }
}

function sendMessage() {
    const text = document.getElementById('messageInput').value.trim();

    if (!lobbyId) {
        alert("Пожалуйста, выберите лобби.");
        return;
    }

    if (!text) {
        alert("Сообщение не может быть пустым.");
        return;
    }

    const message = {
        text: text,
        senderId: senderId,
        lobbyId: lobbyId
    };
    stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
    document.getElementById('messageInput').value = '';
}

function handleKeyDown(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
}

function deleteLobby() {
    console.log('Функция deleteLobby вызвана');
    if (!lobbyId) {
        return;
    }

    if (!confirm("Вы уверены, что хотите удалить лобби?")) {
        console.log('Удаление лобби отменено пользователем');
        return;
    }

    fetch('/lobby/' + lobbyId + '/delete', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        }
    })
        .then(response => {
            if (response.ok) {
                console.log('Лобби успешно удалено');
                closeChat();
            } else {
                return response.text().then(text => { throw new Error(text); });
            }
        })
        .catch(error => {
            alert('Ошибка при удалении лобби: ' + error.message);
            console.error('Ошибка при удалении лобби:', error);
        });
}

function leaveLobby() {
    if (!lobbyId) {
        console.log('Нет активного лобби. Пропускаем вызов leaveLobby.');
        return Promise.resolve();
    }

    console.log(`Покидаем лобби с ID: ${lobbyId}`);

    return fetch('/lobby/leave', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify({ lobbyId: lobbyId })
    })
        .then(response => {
            if (response.ok) {
                console.log('Успешно покинуто лобби');
                closeChat();
            } else {
                return response.text().then(text => {
                    if (text.includes("Вы не состоите в этом лобби")) {
                        console.warn('Пользователь уже не в лобби. Игнорируем ошибку.');
                        closeChat();
                    } else {
                        throw new Error(text);
                    }
                });
            }
        })
        .catch(error => {
            alert(`Ошибка при покидании лобби: ${error.message}`);
            console.error('Ошибка при покидании лобби:', error);
            throw error;
        });
}

function closeChat() {
    if (stompSubscription) {
        stompSubscription.unsubscribe();
        stompSubscription = null;
    }
    if (participantSubscription) {
        participantSubscription.unsubscribe();
        participantSubscription = null;
    }

    document.querySelector('.chat-container').style.display = 'none';
    lobbyId = null;
    currentLobbyCreatorId = null;
    document.getElementById('currentLobbyName').innerText = '';
    document.getElementById('deleteLobbyButton').style.display = 'none';
    document.getElementById('participantsList').innerHTML = '';

    const leaveLobbyButton = document.querySelector('.chat-header .leave-lobby-button');
    leaveLobbyButton.style.display = 'none';
}

function logout() {
    localStorage.removeItem('jwtToken');
    window.location.href = '/login.html';
}

window.onload = function() {
    if (!stompClient) {
        connect();
        loadLobbyList();
    }
}

function joinLobby(lobbyId) {
    return fetch(`/lobby/${lobbyId}/join`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
        .then(response => {
            if (response.ok) {
                return;
            } else {
                return response.text().then(text => { throw new Error(text); });
            }
        })
        .catch(error => {
            alert(`Ошибка при присоединении к лобби: ${error.message}`);
            console.error('Ошибка при присоединении к лобби:', error);
            throw error;
        });
}

function removeLobbyFromList(lobbyIdToRemove, lobbyName) {
    const lobbyElement = document.getElementById('lobby-' + lobbyIdToRemove);
    if (lobbyElement) {
        lobbyElement.remove();
        if (lobbyIdToRemove === lobbyId) {
            closeChat();
        }
    } else {
        console.warn('Лобби с id=' + lobbyIdToRemove + ' не найдено в списке.');
    }
}

function loadParticipants(lobbyId) {
    fetch('/lobby/' + lobbyId + '/participants', {
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        }
    })
        .then(response => response.json())
        .then(data => {
            const participantsList = document.getElementById('participantsList');
            participantsList.innerHTML = '';
            data.forEach(function(participant) {
                const participantElement = document.createElement('li');
                participantElement.className = 'participant';
                participantElement.id = 'participant-' + participant.userId;

                if (participant.userId === currentLobbyCreatorId) {
                    participantElement.classList.add('admin');
                }

                participantElement.textContent = participant.nickname;
                participantsList.appendChild(participantElement);
            });
        })
        .catch(error => console.error('Ошибка при загрузке участников:', error));
}

function addParticipant(participant) {
    const participantsList = document.getElementById('participantsList');
    if (document.getElementById('participant-' + participant.userId)) {
        console.warn('Участник с id=' + participant.userId + ' уже существует в списке.');
        return;
    }

    const participantElement = document.createElement('li');
    participantElement.className = 'participant';
    participantElement.id = 'participant-' + participant.userId;

    if (participant.userId === currentLobbyCreatorId) {
        participantElement.classList.add('admin');
    }

    participantElement.textContent = participant.nickname;
    participantsList.appendChild(participantElement);
}

function removeParticipant(userId) {
    const participantElement = document.getElementById('participant-' + userId);
    if (participantElement) {
        participantElement.remove();
    }
}

function showCreateLobbyForm() {
    const form = document.getElementById('createLobbyForm');
    form.classList.add('active');
    document.getElementById('createLobbyButton').style.display = 'none';
}

function hideCreateLobbyForm() {
    const form = document.getElementById('createLobbyForm');
    form.classList.remove('active');
    document.getElementById('createLobbyButton').style.display = 'block';
    document.getElementById('lobbyNameInput').value = '';
}

function createLobby() {
    const lobbyName = document.getElementById('lobbyNameInput').value.trim();
    if (!lobbyName) {
        alert('Пожалуйста, введите название лобби.');
        return;
    }

    const createButton = document.querySelector('.create-lobby-form .btn-primary');
    createButton.disabled = true;
    createButton.textContent = 'Создаётся...';

    fetch('/lobby/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + jwtToken
        },
        body: JSON.stringify({ name: lobbyName })
    })
        .then(response => {
            if (response.ok) {
                hideCreateLobbyForm();
            } else {
                return response.text().then(text => { throw new Error(text); });
            }
        })
        .catch(error => {
            alert('Ошибка при создании лобби: ' + error.message);
            console.error('Ошибка при создании лобби:', error);
        })
        .finally(() => {
            createButton.disabled = false;
            createButton.textContent = 'Создать';
        });
}
