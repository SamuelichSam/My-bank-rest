# Система управления банковскими картами

# Bank Cards Management System 🏦

REST API для управления банковскими картами с JWT аутентификацией и ролевой моделью доступа. Система предоставляет полный функционал для создания, просмотра, блокировки карт и переводов между счетами.

# ✨ Возможности

- 🔐 JWT аутентификация и авторизация
- 👥 Ролевая модель (USER, ADMIN)
- 💳 Создание и управление банковскими картами
- 🔄 Переводы между собственными картами
- 🛡️ Безопасность данных (шифрование, маскирование)
- 📊 Пагинация и поиск
- 📚 Полная документация OpenAPI/Swagger
- 🐳 Docker контейнеризация

# 🏗️ Архитектура

- **Java 17+** с Spring Boot 3.2+
- **PostgreSQL** для хранения данных
- **Spring Security** с JWT
- **Liquibase** для миграций БД
- **Spring Data JPA** для работы с данными
- **OpenAPI 3.0** для документации

# 🚀 Быстрый старт

## Предварительные требования

- Java 17 или новее
- Maven 3.6+
- PostgreSQL 12+
- Docker и Docker Compose (опционально)

## 1. Клонирование и сборка

```bash
git clone <git@github.com:SamuelichSam/My-bank-rest.git>
cd Bank_REST
mvn clean package
```

## 2. Настройка базы данных

```bash
# Создание БД
createdb bank_cards_db

# Или через Docker
docker run --name postgres \
  -e POSTGRES_DB=bank_cards_db \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

## 3. Запуск приложения

```bash
# Development режим
java -jar target/bank-cards.jar --spring.profiles.active=dev

# Или через Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 4. Docker запуск

```bash
# Сборка и запуск всех сервисов
docker-compose up --build

# Запуск в фоновом режиме
docker-compose up -d

# Остановка
docker-compose down
```

## 📖 Документация API
После запуска приложения доступны:

Swagger UI: http://localhost:8080/swagger-ui.html

OpenAPI JSON: http://localhost:8080/v3/api-docs

## Тестовые данные
### Автоматически создаются при запуске:

#### Администратор:

- Логин: admin

- Пароль: admin123

- Роль: ADMIN

#### Пользователи:

- Логин: user1 / Пароль: user123 → Карты: IVAN IVANOV

- Логин: user2 / Пароль: user123 → Карты: PETR PETROV