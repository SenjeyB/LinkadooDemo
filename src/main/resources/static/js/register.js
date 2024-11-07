document.getElementById('registerForm').addEventListener('submit', function(event){
    event.preventDefault();

    var username = document.getElementById('username').value;
    var nickname = document.getElementById('nickname').value;
    var password = document.getElementById('password').value;
    var confirmPassword = document.getElementById('confirmPassword').value;

    if (password !== confirmPassword) {
        alert('Пароли не совпадают');
        return;
    }

    fetch('/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username: username,
            nickname: nickname,
            password: password
        })

    })
        .then(response => {
            if (response.ok) {
                alert('Регистрация успешна. Теперь вы можете войти в систему.');
                window.location.href = '/login.html';
            } else {
                return response.text().then(text => { throw new Error(text); });
            }
        })
        .catch(error => {
            alert('Ошибка при регистрации: ' + error.message);
            console.error('Ошибка при регистрации:', error);
        });
});
