(function() {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        document.documentElement.classList.add('dark-theme');
    } else {
        document.documentElement.classList.remove('dark-theme');
    }
})();

function toggleTheme() {
    const toggle = document.getElementById('themeToggle');
    const themeIcon = document.getElementById('themeIcon');
    document.documentElement.classList.toggle('dark-theme');
    if (document.documentElement.classList.contains('dark-theme')) {
        localStorage.setItem('theme', 'dark');
        if (toggle) toggle.checked = true;
        if (themeIcon) themeIcon.textContent = '🌙';
    } else {
        localStorage.setItem('theme', 'light');
        if (toggle) toggle.checked = false;
        if (themeIcon) themeIcon.textContent = '☀️';
    }
}
document.addEventListener('DOMContentLoaded', function() {
    const savedTheme = localStorage.getItem('theme');
    const toggle = document.getElementById('themeToggle');
    const themeIcon = document.getElementById('themeIcon');
    if (savedTheme === 'dark') {
        document.documentElement.classList.add('dark-theme');
        if (toggle) toggle.checked = true;
        if (themeIcon) themeIcon.textContent = '🌙';
    } else {
        document.documentElement.classList.remove('dark-theme');
        if (toggle) toggle.checked = false;
        if (themeIcon) themeIcon.textContent = '☀️';
    }
});
