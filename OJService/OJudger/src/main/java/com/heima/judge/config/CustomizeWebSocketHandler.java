package com.heima.judge.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class CustomizeWebSocketHandler extends TextWebSocketHandler {


    private Logger log = LoggerFactory.getLogger(CustomizeWebSocketHandler.class);
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<String, WebSocketSession> sessionsmap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("与客户端建立连接...");
        //连接成功时调用该方法
        System.out.println("WebSocket connected: " + session.getId());

        String uri = session.getUri().toString();

        String userId = uri.substring(uri.indexOf("=") + 1, uri.indexOf("&"));
        sessions.add(session);
        sessionsmap.put(userId, session);
    }


    public void sendall(String s) throws Exception {
        for (WebSocketSession ws : sessionsmap.values()) {
            if (ws != null) handleMessage(ws, new TextMessage(s.getBytes()));
        }
    }

    public void sendtoone(String id, String s) throws Exception {
        if (sessionsmap.containsKey(id)) handleMessage(sessionsmap.get(id), new TextMessage(s.getBytes()));
    }


    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // 获取客户端发送的消息
        System.out.println("客户端ID: " + session.getId() + " 发送消息: " + message.getPayload());

        session.sendMessage(message);

//        for (WebSocketSession s : sessions) {
//            System.out.println(s);
//            System.out.println(session);

//            if (session.isOpen()) {
//                String response = "服务端响应: " + message.getPayload();
//                // 发送消息给客户端
//                session.sendMessage(new TextMessage(response));
//                // 关闭连接
//                // s.close(CloseStatus.NORMAL);
//            }


    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        //发生错误时调用该方法
        System.err.println("WebSocket error: " + exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
        log.error("连接异常", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        //连接关闭时调用该方法
        System.out.println("WebSocket closed: " + session.getId());
        sessions.remove(session);
        super.afterConnectionClosed(session, closeStatus);
        log.info("与客户端断开连接...");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


}
