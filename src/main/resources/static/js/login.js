document.getElementById('loginForm').addEventListener('submit', function(event){
    event.preventDefault();

    var username = document.getElementById('username').value;
    var password = document.getElementById('password').value;

    fetch('/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({username: username, password: password})
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(errData => {
                    throw new Error(errData.message || 'Ошибка при входе в систему');
                });
            }
            return response.json();
        })
        .then(data => {
            console.log('Ответ от сервера при логине:', data);
            if (data.jwtToken) {
                localStorage.setItem('jwtToken', data.jwtToken);
                console.log('Токен сохранён:', data.jwtToken);
                window.location.href = '/index.html';
            } else if (data.token) {
                localStorage.setItem('jwtToken', data.token);
                console.log('Токен сохранён:', data.token);
                window.location.href = '/index.html';
            } else {
                alert('Неверное имя пользователя или пароль');
            }
        })
        .catch(error => {
            console.error('Ошибка при входе в систему:', error);
            alert('Ошибка при входе в систему: ' + error.message);
        });
});
