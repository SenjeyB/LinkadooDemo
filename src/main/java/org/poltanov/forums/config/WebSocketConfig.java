package org.poltanov.forums.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Конфигурация WebSocket для приложения.
 * Настраивает брокер сообщений и конечные точки STOMP.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    /**
     * Конструктор для создания экземпляра WebSocketConfig.
     *
     * @param jwtHandshakeInterceptor перехватчик рукопожатия для проверки JWT-токенов
     */
    public WebSocketConfig(JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    /**
     * Конфигурирует брокер сообщений.
     *
     * @param config объект {@link MessageBrokerRegistry} для настройки брокера сообщений
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Регистрирует конечные точки STOMP.
     *
     * @param registry объект {@link StompEndpointRegistry} для регистрации конечных точек
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor)
                .withSockJS();
    }
}
