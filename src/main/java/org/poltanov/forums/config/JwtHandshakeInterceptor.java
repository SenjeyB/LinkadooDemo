package org.poltanov.forums.config;

import org.poltanov.forums.util.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Перехватчик рукопожатия WebSocket, который проверяет JWT-токен.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    /**
     * Конструктор для создания экземпляра JwtHandshakeInterceptor.
     *
     * @param jwtUtil утилита для работы с JWT-токенами
     */
    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Выполняет проверку JWT-токена перед установлением WebSocket соединения.
     *
     * @param request    HTTP-запрос
     * @param response   HTTP-ответ
     * @param wsHandler  обработчик WebSocket
     * @param attributes атрибуты, которые будут доступны в WebSocket-сессии
     * @return {@code true}, если рукопожатие разрешено; {@code false} в противном случае
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        String token = null;
        String query = request.getURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    token = param.substring(6);
                    break;
                }
            }
        }

        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            attributes.put("username", username);
            return true;
        }

        return false;
    }

    /**
     * Метод вызывается после завершения рукопожатия WebSocket. В данном случае не выполняет никаких действий.
     *
     * @param request    HTTP-запрос
     * @param response   HTTP-ответ
     * @param wsHandler  обработчик WebSocket
     * @param exception исключение, возникшее во время рукопожатия, или {@code null}, если исключений не было
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
