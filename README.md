# LinkadooDemo

**LinkadooDemo** — демонстрационный проект веб-чата, основанного на старомодных сервисах лобби. Проект демонстрирует работу системы обмена сообщениями в реальном времени с использованием классических подходов к организации лобби-сервисов.

## Особенности

- Реализация веб-чата с возможностью обмена сообщениями в реальном времени.
- Серверная часть написана на Java.
- Клиентская часть реализована с использованием HTML, CSS и JavaScript.
- Применение Maven для сборки и управления зависимостями (используется Maven Wrapper).

## Структура проекта

- **src** – исходный код приложения.
- **Docs** – документация и дополнительные материалы.
- **pom.xml** – файл конфигурации Maven.
- **mvnw / mvnw.cmd** – скрипты Maven Wrapper для Unix/Windows.

## Требования

- Java JDK 8 или выше.
- Maven (необязательно, так как используется Maven Wrapper).
- Веб-браузер для доступа к клиентской части.

## Установка и запуск

1. **Клонирование репозитория:**

   ```bash
   git clone https://github.com/SenjeyB/LinkadooDemo.git
   ```

2. **Сборка проекта:**

   Для Unix-систем:
   ```bash
   ./mvnw clean install
   ```
   Для Windows:
   ```bash
   mvnw.cmd clean install
   ```

3. **Запуск приложения:**

   Воспользуйтесь инструментами Spring.
   
   Для Unix-систем:
   ```bash
   ./mvnw spring-boot:run
   ```
   Для Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

После запуска откройте веб-браузер и перейдите по адресу: http://localhost:8080


## Документация

Дополнительную информацию по настройке и работе с проектом можно найти в каталоге **Docs**.

## Сайт

В данный момент последняя версия сайта (необязательно совпадает с той, что доуступна в репозиторий) доступна тут: [https://linkadoo-chat.ru](https://linkadoo-chat.ru)
