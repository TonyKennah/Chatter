package uk.co.kennah.chatter.socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String username = getUsernameFromUri(session);
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Connection established but no username found in URI: {}. Closing session.", session.getUri());
            session.close(CloseStatus.BAD_DATA.withReason("Username is required."));
            return;
        }
        session.getAttributes().put("username", username);

        String roomId = getRoomIdFromUri(session);
        if (roomId == null) {
            logger.warn("Connection established but no roomId found in URI: {}. Closing session.", session.getUri());
            session.close(CloseStatus.BAD_DATA.withReason("Room ID is required."));
            return;
        }

        // Store the decoded room ID in the session attributes for later use
        session.getAttributes().put("decodedRoomId", roomId);

        logger.info("New WebSocket connection established for room: {}, user: '{}', session: {}", roomId, username, session.getId());
        rooms.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);

        // Broadcast to the room that a new user has joined
        String joinMessage = objectMapper.writeValueAsString(Map.of("type", "info", "payload", "User '" + username + "' has joined."));
        broadcastToRoom(roomId, joinMessage);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String roomId = (String) session.getAttributes().get("decodedRoomId");
        String username = (String) session.getAttributes().get("username");
        if (roomId != null) {
            logger.info("Connection closed for room: {}. Session ID: {}. Status: {}", roomId, session.getId(), status);
            Set<WebSocketSession> sessions = rooms.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    rooms.remove(roomId);
                    logger.info("Room {} is now empty and has been removed.", roomId);
                } else {
                    // Broadcast to the room that a user has left
                    if (username != null) {
                        String leaveMessage = objectMapper.writeValueAsString(Map.of("type", "info", "payload", "User '" + username + "' has left."));
                        broadcastToRoom(roomId, leaveMessage);
                    }
                }
            }
        } else {
            logger.warn("Connection closed for session {} with no roomId.", session.getId());
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws IOException {
        String roomId = (String) session.getAttributes().get("decodedRoomId");
        String username = (String) session.getAttributes().get("username");
        if (roomId == null || username == null) {
            return; // Should not happen if connection was established correctly
        }

        logger.info("Received message: {} from user: {} in room: {}", message.getPayload(), username, roomId);

        Map<String, String> messageMap;
        try {
            messageMap = objectMapper.readValue(message.getPayload(), new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            logger.warn("Invalid JSON received: {}", message.getPayload());
            return;
        }

        String messageType = messageMap.get("type");
        if (messageType == null) {
            logger.warn("Message received without type: {}", message.getPayload());
            return;
        }

        switch (messageType) {
            case "chat":
                String payload = messageMap.get("payload");
                if (payload == null) return;

                if (payload.startsWith("/")) {
                    handleCommand(session, payload);
                } else {
                    Map<String, String> broadcastMessage = Map.of("type", "chat", "user", username, "payload", payload);
                    String messageJson = objectMapper.writeValueAsString(broadcastMessage);
                    broadcastToRoom(roomId, messageJson);
                }
                break;
            case "typing":
                Map<String, String> broadcastMessage = Map.of("type", "typing", "user", username);
                String messageJson = objectMapper.writeValueAsString(broadcastMessage);
                broadcastToOthersInRoom(roomId, session, messageJson);
                break;
            default:
                logger.warn("Unknown message type received: {}", messageType);
        }
    }

    private void handleCommand(WebSocketSession session, String commandPayload) throws IOException {
        String[] parts = commandPayload.split(" ", 2);
        String command = parts[0];

        switch (command) {
            case "/me":
                handleMeCommand(session);
                break;
            default:
                String unknownCommandMessage = "Unknown command: " + command;
                Map<String, String> response = Map.of("type", "info", "payload", unknownCommandMessage);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                break;
        }
    }

    private void handleMeCommand(WebSocketSession session) throws IOException {
        String username = (String) session.getAttributes().get("username");
        if (username == null) return;

        String serverTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String responsePayload = "Hello " + username + "! Current Server time is " + serverTime;

        Map<String, String> response = Map.of("type", "info", "payload", responsePayload);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void broadcastToRoom(String roomId, String message) throws IOException {
        Set<WebSocketSession> sessions = rooms.get(roomId);
        if (sessions == null) return;
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) session.sendMessage(new TextMessage(message));
        }
    }

    private void broadcastToOthersInRoom(String roomId, WebSocketSession excludeSession, String message) throws IOException {
        Set<WebSocketSession> sessions = rooms.get(roomId);
        if (sessions == null) return;
        for (WebSocketSession session : sessions) {
            if (session.isOpen() && !session.getId().equals(excludeSession.getId())) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    private String getRoomIdFromUri(WebSocketSession session) {
        if (session.getUri() == null) {
            return null;
        }
        try {
            // Manually parse the URI to extract the last path segment as the room ID.
            // Example URI: /chat-ws/general or /chat-ws/tech%20talk
            List<String> pathSegments = UriComponentsBuilder.fromUri(session.getUri()).build().getPathSegments();
            String encodedRoomId = pathSegments.get(pathSegments.size() - 1);
            // The ID is URL-encoded by the client, so we must decode it.
            return URLDecoder.decode(encodedRoomId, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            logger.error("Failed to extract and decode roomId from URI: {}", session.getUri(), e);
            return null;
        }
    }

    private String getUsernameFromUri(WebSocketSession session) {
        if (session.getUri() == null) {
            return null;
        }
        try {
            Map<String, String> queryParams = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams().toSingleValueMap();
            String username = queryParams.get("username");
            if (username != null) {
                return URLDecoder.decode(username, StandardCharsets.UTF_8.name());
            }
            return null;
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to decode username from URI: {}", session.getUri(), e);
            return null;
        }
    }
}