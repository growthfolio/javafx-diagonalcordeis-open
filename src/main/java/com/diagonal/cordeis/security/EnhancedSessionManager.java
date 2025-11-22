package com.diagonal.cordeis.security;

import com.diagonal.cordeis.model.User;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class EnhancedSessionManager {
    
    @Value("${security.session.timeout-minutes:30}")
    private int sessionTimeoutMinutes;
    
    @Value("${security.session.max-concurrent:3}")
    private int maxConcurrentSessions;
    
    @Value("${security.session.persistent:true}")
    private boolean persistentSessions;
    
    @Autowired
    private EnhancedAuditService auditService;
    
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, List<String>> userSessions = new ConcurrentHashMap<>();
    
    private static final String SESSION_FILE = "sessions.dat";
    private static final String SESSION_KEY_FILE = "session.key";
    private SecretKey encryptionKey;
    
    public EnhancedSessionManager() {
        initializeEncryption();
        loadPersistedSessions();
    }
    
    public synchronized String createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        
        if (!canCreateNewSession(user.getUsername())) {
            removeOldestSession(user.getUsername());
        }
        
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setSessionId(sessionId);
        sessionInfo.setUsername(user.getUsername());
        sessionInfo.setUserId(user.getId());
        sessionInfo.setCreatedAt(LocalDateTime.now());
        sessionInfo.setLastAccessAt(LocalDateTime.now());
        sessionInfo.setActive(true);
        
        activeSessions.put(sessionId, sessionInfo);
        userSessions.computeIfAbsent(user.getUsername(), k -> new ArrayList<>()).add(sessionId);
        
        if (persistentSessions) {
            try {
                persistSessions();
            } catch (Exception e) {
                log.warn("Failed to persist sessions: {}", e.getMessage());
            }
        }
        
        try {
            auditService.logAction(user.getUsername(), "SESSION_CREATED", "Session created: " + sessionId);
        } catch (Exception e) {
            log.warn("Failed to log session creation: {}", e.getMessage());
        }
        
        log.info("Created new session for user: {} (ID: {})", user.getUsername(), sessionId);
        return sessionId;
    }
    
    public synchronized boolean isSessionValid(String sessionId) {
        if (sessionId == null) return false;
        
        SessionInfo session = activeSessions.get(sessionId);
        if (session == null || !session.isActive()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastAccess = ChronoUnit.MINUTES.between(session.getLastAccessAt(), now);
        
        if (minutesSinceLastAccess > sessionTimeoutMinutes) {
            invalidateSession(sessionId);
            try {
                auditService.logAction(session.getUsername(), "SESSION_EXPIRED", 
                    "Session expired after " + minutesSinceLastAccess + " minutes");
            } catch (Exception e) {
                log.warn("Failed to log session expiration: {}", e.getMessage());
            }
            return false;
        }
        
        session.setLastAccessAt(now);
        if (persistentSessions) {
            try {
                persistSessions();
            } catch (Exception e) {
                log.warn("Failed to persist sessions: {}", e.getMessage());
            }
        }
        
        return true;
    }
    
    public synchronized void invalidateSession(String sessionId) {
        if (sessionId == null) return;
        
        SessionInfo session = activeSessions.remove(sessionId);
        if (session != null) {
            session.setActive(false);
            
            List<String> userSessionList = userSessions.get(session.getUsername());
            if (userSessionList != null) {
                userSessionList.remove(sessionId);
                if (userSessionList.isEmpty()) {
                    userSessions.remove(session.getUsername());
                }
            }
            
            try {
                auditService.logAction(session.getUsername(), "SESSION_INVALIDATED", "Session invalidated: " + sessionId);
            } catch (Exception e) {
                log.warn("Failed to log session invalidation: {}", e.getMessage());
            }
            
            log.info("Session invalidated: {} for user: {}", sessionId, session.getUsername());
        }
        
        if (persistentSessions) {
            try {
                persistSessions();
            } catch (Exception e) {
                log.warn("Failed to persist sessions: {}", e.getMessage());
            }
        }
    }
    
    public List<SessionInfo> getActiveSessionsForUser(String username) {
        List<String> sessionIds = userSessions.get(username);
        if (sessionIds == null) return new ArrayList<>();
        
        return sessionIds.stream()
            .map(activeSessions::get)
            .filter(Objects::nonNull)
            .filter(SessionInfo::isActive)
            .toList();
    }
    
    private boolean canCreateNewSession(String username) {
        return getActiveSessionsForUser(username).size() < maxConcurrentSessions;
    }
    
    private void removeOldestSession(String username) {
        List<SessionInfo> userActiveSessions = getActiveSessionsForUser(username);
        if (!userActiveSessions.isEmpty()) {
            SessionInfo oldest = userActiveSessions.stream()
                .min(Comparator.comparing(SessionInfo::getCreatedAt))
                .orElse(null);
            
            if (oldest != null) {
                invalidateSession(oldest.getSessionId());
                auditService.logAction(username, "SESSION_REPLACED", "Oldest session removed due to concurrent limit");
            }
        }
    }
    
    @Scheduled(fixedRate = 300000) // 5 minutos
    public void cleanupExpiredSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minus(sessionTimeoutMinutes, ChronoUnit.MINUTES);
        
        List<String> expiredSessions = activeSessions.entrySet().stream()
            .filter(entry -> entry.getValue().getLastAccessAt().isBefore(cutoff))
            .map(Map.Entry::getKey)
            .toList();
        
        for (String sessionId : expiredSessions) {
            invalidateSession(sessionId);
        }
        
        if (!expiredSessions.isEmpty()) {
            log.info("Cleaned up {} expired sessions", expiredSessions.size());
        }
    }
    
    private void initializeEncryption() {
        try {
            Path keyPath = Paths.get(SESSION_KEY_FILE);
            if (Files.exists(keyPath)) {
                byte[] keyBytes = Files.readAllBytes(keyPath);
                encryptionKey = new SecretKeySpec(keyBytes, "AES");
            } else {
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(128); // Reduzido para 128 bits para compatibilidade
                encryptionKey = keyGen.generateKey();
                Files.write(keyPath, encryptionKey.getEncoded());
            }
        } catch (Exception e) {
            log.error("Erro ao inicializar criptografia de sessões", e);
            encryptionKey = new SecretKeySpec("DiagonalCordeis1".getBytes(), "AES");
        }
    }
    
    private void persistSessions() {
        if (!persistentSessions) return;
        
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, SessionInfo> entry : activeSessions.entrySet()) {
                SessionInfo session = entry.getValue();
                sb.append(session.getSessionId()).append("|");
                sb.append(session.getUsername()).append("|");
                sb.append(session.getUserId()).append("|");
                sb.append(session.getCreatedAt()).append("|");
                sb.append(session.getLastAccessAt()).append("|");
                sb.append(session.isActive()).append("\n");
            }
            byte[] encrypted = encrypt(sb.toString().getBytes());
            Files.write(Paths.get(SESSION_FILE), encrypted);
        } catch (Exception e) {
            log.error("Erro ao persistir sessões", e);
        }
    }
    
    private void loadPersistedSessions() {
        if (!persistentSessions) return;
        
        try {
            Path sessionPath = Paths.get(SESSION_FILE);
            if (Files.exists(sessionPath)) {
                byte[] encrypted = Files.readAllBytes(sessionPath);
                byte[] decrypted = decrypt(encrypted);
                String data = new String(decrypted);
                
                String[] lines = data.split("\n");
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    
                    String[] parts = line.split("\\|");
                    if (parts.length >= 6) {
                        SessionInfo session = new SessionInfo();
                        session.setSessionId(parts[0]);
                        session.setUsername(parts[1]);
                        session.setUserId(Long.parseLong(parts[2]));
                        session.setCreatedAt(LocalDateTime.parse(parts[3]));
                        session.setLastAccessAt(LocalDateTime.parse(parts[4]));
                        session.setActive(Boolean.parseBoolean(parts[5]));
                        
                        if (session.isActive()) {
                            activeSessions.put(session.getSessionId(), session);
                            userSessions.computeIfAbsent(session.getUsername(), k -> new ArrayList<>())
                                .add(session.getSessionId());
                        }
                    }
                }
                
                log.info("Carregadas {} sessões persistidas", activeSessions.size());
            }
        } catch (Exception e) {
            log.error("Erro ao carregar sessões persistidas", e);
        }
    }
    
    private byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        return cipher.doFinal(data);
    }
    
    private byte[] decrypt(byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey);
        return cipher.doFinal(encryptedData);
    }
    
    @Data
    public static class SessionInfo {
        private String sessionId;
        private String username;
        private Long userId;
        private LocalDateTime createdAt;
        private LocalDateTime lastAccessAt;
        private boolean active;
    }
}