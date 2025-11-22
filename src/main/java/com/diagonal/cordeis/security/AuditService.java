package com.diagonal.cordeis.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuditService {

    /**
     * Log a user action asynchronously
     * @param username The username who performed the action
     * @param action The action performed
     * @param details Additional details about the action
     */
    @Async
    public void logAction(String username, String action, String details) {
        log.info("AUDIT: [{}] {} - {}", username, action, details);
        // This could be expanded to store audit logs in a database
    }

    /**
     * Log a security event (login, logout, access denied, etc.)
     * @param username The username related to the event
     * @param event The security event
     * @param result The result of the event (success, failure, etc.)
     * @param details Additional details
     */
    public void logSecurityEvent(String username, String event, String result, String details) {
        log.warn("SECURITY: [{}] {} - {} - {}", username, event, result, details);
        // This could be expanded to store security events in a database and trigger alerts
    }
}
