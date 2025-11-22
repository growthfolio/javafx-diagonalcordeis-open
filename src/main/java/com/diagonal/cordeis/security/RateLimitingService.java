package com.diagonal.cordeis.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class RateLimitingService {
    
    @Value("${security.rate-limit.login-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${security.rate-limit.window-minutes:15}")
    private int windowMinutes;
    
    @Value("${security.rate-limit.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;
    
    @Autowired
    private EnhancedAuditService auditService;
    
    // Mapa para rastrear tentativas por identificador (username ou IP)
    private final Map<String, List<LocalDateTime>> attemptHistory = new ConcurrentHashMap<>();
    
    // Mapa para rastrear contas bloqueadas
    private final Map<String, LocalDateTime> lockedAccounts = new ConcurrentHashMap<>();
    
    public boolean isRateLimited(String identifier, ActionType actionType) {
        // Verificar se está bloqueado
        if (isLocked(identifier)) {
            log.warn("Rate limit: {} is locked until {}", identifier, 
                lockedAccounts.get(identifier).plus(lockoutDurationMinutes, ChronoUnit.MINUTES));
            return true;
        }
        
        String key = identifier + ":" + actionType;
        List<LocalDateTime> attempts = attemptHistory.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        
        // Remover tentativas antigas
        LocalDateTime cutoff = LocalDateTime.now().minus(windowMinutes, ChronoUnit.MINUTES);
        attempts.removeIf(attempt -> attempt.isBefore(cutoff));
        
        // Verificar limite
        int maxAttempts = getMaxAttemptsForAction(actionType);
        if (attempts.size() >= maxAttempts) {
            lockAccount(identifier);
            auditService.logSecurityEvent(identifier, "RATE_LIMIT_EXCEEDED", "BLOCKED", 
                String.format("Exceeded %d attempts in %d minutes", maxAttempts, windowMinutes));
            return true;
        }
        
        return false;
    }
    
    public void recordAttempt(String identifier, ActionType actionType, boolean success) {
        String key = identifier + ":" + actionType;
        List<LocalDateTime> attempts = attemptHistory.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        attempts.add(LocalDateTime.now());
        
        if (success) {
            // Limpar histórico em caso de sucesso
            attempts.clear();
            unlockAccount(identifier);
        }
        
        log.debug("Recorded {} attempt for {}: {} (total: {})", 
            actionType, identifier, success ? "SUCCESS" : "FAILED", attempts.size());
    }
    
    public int getRemainingAttempts(String identifier, ActionType actionType) {
        if (isLocked(identifier)) return 0;
        
        String key = identifier + ":" + actionType;
        List<LocalDateTime> attempts = attemptHistory.get(key);
        if (attempts == null) return getMaxAttemptsForAction(actionType);
        
        // Remover tentativas antigas
        LocalDateTime cutoff = LocalDateTime.now().minus(windowMinutes, ChronoUnit.MINUTES);
        attempts.removeIf(attempt -> attempt.isBefore(cutoff));
        
        return Math.max(0, getMaxAttemptsForAction(actionType) - attempts.size());
    }
    
    public LocalDateTime getLockoutExpiry(String identifier) {
        LocalDateTime lockTime = lockedAccounts.get(identifier);
        return lockTime != null ? lockTime.plus(lockoutDurationMinutes, ChronoUnit.MINUTES) : null;
    }
    
    private boolean isLocked(String identifier) {
        LocalDateTime lockTime = lockedAccounts.get(identifier);
        if (lockTime == null) return false;
        
        LocalDateTime expiry = lockTime.plus(lockoutDurationMinutes, ChronoUnit.MINUTES);
        if (LocalDateTime.now().isAfter(expiry)) {
            lockedAccounts.remove(identifier);
            log.info("Rate limit lock expired for: {}", identifier);
            return false;
        }
        
        return true;
    }
    
    private void lockAccount(String identifier) {
        lockedAccounts.put(identifier, LocalDateTime.now());
        log.warn("Account locked due to rate limiting: {}", identifier);
    }
    
    private void unlockAccount(String identifier) {
        if (lockedAccounts.remove(identifier) != null) {
            log.info("Account unlocked after successful authentication: {}", identifier);
        }
    }
    
    private int getMaxAttemptsForAction(ActionType actionType) {
        return switch (actionType) {
            case LOGIN -> maxLoginAttempts;
            case PASSWORD_RESET -> 3;
            case ACCOUNT_CREATION -> 3;
            default -> 5;
        };
    }
    
    @Scheduled(fixedRate = 300000) // 5 minutos
    public void cleanupExpiredEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minus(windowMinutes * 2, ChronoUnit.MINUTES);
        
        // Limpar histórico de tentativas antigas
        attemptHistory.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(attempt -> attempt.isBefore(cutoff));
            return entry.getValue().isEmpty();
        });
        
        // Limpar bloqueios expirados
        LocalDateTime lockCutoff = LocalDateTime.now().minus(lockoutDurationMinutes, ChronoUnit.MINUTES);
        lockedAccounts.entrySet().removeIf(entry -> entry.getValue().isBefore(lockCutoff));
        
        log.debug("Rate limiting cleanup completed");
    }
    
    public enum ActionType {
        LOGIN, PASSWORD_RESET, ACCOUNT_CREATION, API_REQUEST
    }
}