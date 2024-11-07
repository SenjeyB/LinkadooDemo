package org.poltanov.forums.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Утилитный класс для создания и валидации JWT токенов.
 * Предоставляет методы для генерации токенов, извлечения информации из них и проверки их валидности.
 */
@Component
public class JwtUtil {

    /**
     * Секретный ключ для подписывания JWT токенов.
     * <p>
     * Должен быть не короче 32 символов для алгоритма HS256 и закодирован в Base64.
     * </p>
     */
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    /**
     * Получает объект {@link Key} для подписи JWT токенов.
     *
     * @return {@link Key} для подписи токенов.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Извлекает имя пользователя из JWT токена.
     *
     * @param token JWT токен.
     * @return Имя пользователя.
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Извлекает дату истечения срока действия JWT токена.
     *
     * @param token JWT токен.
     * @return Дата истечения срока действия.
     */
    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    /**
     * Извлекает все утверждения (claims) из JWT токена.
     *
     * @param token JWT токен.
     * @return {@link Claims} содержащие все утверждения токена.
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Проверяет, истёк ли срок действия JWT токена.
     *
     * @param token JWT токен.
     * @return {@code true}, если токен истёк, иначе {@code false}.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Генерирует JWT токен для аутентифицированного пользователя.
     *
     * @param userDetails {@link UserDetails} информации о пользователе.
     * @param userId      ID пользователя.
     * @param nickname    Никнейм пользователя.
     * @return Сгенерированный JWT токен.
     */
    public String generateToken(UserDetails userDetails, Long userId, String nickname) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("nickname", nickname);
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Создаёт JWT токен с заданными утверждениями и субъектом.
     *
     * @param claims  Утверждения для включения в токен.
     * @param subject Субъект токена (обычно имя пользователя).
     * @return Сгенерированный JWT токен.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 часов
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Валидирует JWT токен.
     *
     * @param token JWT токен для валидации.
     * @return {@code true}, если токен валиден, иначе {@code false}.
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Логирование ошибки можно добавить при необходимости
            return false;
        }
    }
}
