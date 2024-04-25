package com.heima.judge.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket//开启WebSocket相关功能
public class WebSocketServerConfigure implements WebSocketConfigurer {

    @Autowired
    private CustomizeWebSocketHandler customizeWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册自定义customizeWebSocketHandler 使用ws
        registry.addHandler(customizeWebSocketHandler, "/ws").setAllowedOrigins("*").withSockJS();
    }
}
