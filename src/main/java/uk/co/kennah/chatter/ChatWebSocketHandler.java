package uk.co.kennah.chatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws JsonProcessingException, IOException {
        sessions.add(session);
        logger.info("New WebSocket connection established: {}", session.getId());
        Map<String, String> message = Map.of(
                "type", "info",
                "payload", "User " + getShortId(session.getId()) + " joined"
        );
        broadcast(objectMapper.writeValueAsString(message));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        logger.info("Received message: {} from session: {}", message.getPayload(), session.getId());
        Map<String, String> messageMap = objectMapper.readValue(message.getPayload(), Map.class);
        String messageType = messageMap.get("type");
        String userId = getShortId(session.getId());

        Map<String, String> broadcastMessage;

        switch (messageType) {
            case "chat":
                broadcastMessage = Map.of(
                        "type", "chat",
                        "user", userId,
                        "payload", messageMap.get("payload")
                );
                broadcastToOthers(session, objectMapper.writeValueAsString(broadcastMessage));
                break;
            case "typing":
                broadcastMessage = Map.of(
                        "type", "typing",
                        "user", userId
                );
                broadcastToOthers(session, objectMapper.writeValueAsString(broadcastMessage));
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws JsonProcessingException, IOException {
        sessions.remove(session);
        logger.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
        Map<String, String> message = Map.of(
                "type", "info",
                "payload", "User " + getShortId(session.getId()) + " left"
        );
        broadcast(objectMapper.writeValueAsString(message));
    }

    private void broadcast(String message) throws IOException {
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(message));
            }
        }
    }

    private void broadcastToOthers(WebSocketSession sender, String message) throws IOException {
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen() && !sender.getId().equals(webSocketSession.getId())) {
                webSocketSession.sendMessage(new TextMessage(message));
            }
        }
    }

    private String getShortId(String sessionId) {
        // Return first 8 chars for a shorter, anonymous ID
        return sessionId.substring(0, 8);
    }
}