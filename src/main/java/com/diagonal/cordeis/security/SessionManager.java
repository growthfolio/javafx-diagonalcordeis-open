package com.diagonal.cordeis.security;

import com.diagonal.cordeis.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionManager {
    private static final long SESSION_TIMEOUT_MINUTES = 30; // 30 minutos

    private final Map<String, LocalDateTime> userSessions = new ConcurrentHashMap<>();

    /**
     * Create a new session for a user
     * @param user The authenticated user
     * @return The session ID
     */
    public String createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        user.setSessionId(sessionId);
        user.setLastLogin(LocalDateTime.now());
        userSessions.put(sessionId, LocalDateTime.now());
        log.info("Created new session for user: {}", user.getUsername());
        return sessionId;
    }

    /**
     * Check if a session is valid and update last access time
     * @param sessionId The session ID to validate
     * @return true if session is valid, false otherwise
     */
    public boolean isSessionValid(String sessionId) {
        if (sessionId == null || !userSessions.containsKey(sessionId)) {
            return false;
        }

        LocalDateTime lastAccess = userSessions.get(sessionId);
        if (ChronoUnit.MINUTES.between(lastAccess, LocalDateTime.now()) > SESSION_TIMEOUT_MINUTES) {
            // Session expired, remove it
            userSessions.remove(sessionId);
            log.info("Session expired: {}", sessionId);
            return false;
        }

        // Update last access time
        userSessions.put(sessionId, LocalDateTime.now());
        return true;
    }

    /**
     * Invalidate a user session
     * @param sessionId The session ID to invalidate
     */
    public void invalidateSession(String sessionId) {
        if (sessionId != null) {
            userSessions.remove(sessionId);
            log.info("Session invalidated: {}", sessionId);
        }
    }
}
