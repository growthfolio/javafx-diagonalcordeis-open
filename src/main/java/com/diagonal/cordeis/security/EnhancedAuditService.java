package com.diagonal.cordeis.security;

import com.diagonal.cordeis.repository.SecurityAuditRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class EnhancedAuditService {
    
    @Autowired
    private SecurityAuditRepository auditRepository;
    
    @Value("${security.audit.enabled:true}")
    private boolean auditEnabled;
    
    @Value("${security.audit.retention-days:90}")
    private int retentionDays;
    
    @Value("${security.audit.alert-on-suspicious:true}")
    private boolean alertOnSuspicious;
    
    @Async
    public void logSecurityEvent(String username, String action, String result, String details) {
        if (!auditEnabled) return;
        
        try {
            SecurityAuditLog.RiskLevel riskLevel = determineRiskLevel(action, result, username);
            
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                .username(username)
                .action(action)
                .timestamp(LocalDateTime.now())
                .success("SUCCESS".equals(result))
                .details(details)
                .riskLevel(riskLevel)
                .build();
            
            auditRepository.save(auditLog);
            
            // Log estruturado
            log.info("SECURITY_AUDIT: [{}] {} - {} - {} - Risk: {}", 
                username, action, result, details, riskLevel);
            
            // Detectar atividades suspeitas
            if (alertOnSuspicious && isSuspiciousActivity(username, action, result)) {
                handleSuspiciousActivity(username, action, details);
            }
            
        } catch (Exception e) {
            log.error("Erro ao registrar evento de auditoria", e);
        }
    }
    
    @Async
    public void logAction(String username, String action, String details) {
        logSecurityEvent(username, action, "SUCCESS", details);
    }
    
    private SecurityAuditLog.RiskLevel determineRiskLevel(String action, String result, String username) {
        // Ações de alto risco
        if (action.contains("ADMIN") || action.contains("DELETE") || action.contains("MODIFY_PERMISSIONS")) {
            return SecurityAuditLog.RiskLevel.HIGH;
        }
        
        // Falhas de autenticação
        if ("FAILED".equals(result) && (action.contains("LOGIN") || action.contains("AUTH"))) {
            return SecurityAuditLog.RiskLevel.MEDIUM;
        }
        
        // Bloqueios e violações
        if (action.contains("BLOCKED") || action.contains("RATE_LIMIT") || action.contains("VIOLATION")) {
            return SecurityAuditLog.RiskLevel.HIGH;
        }
        
        return SecurityAuditLog.RiskLevel.LOW;
    }
    
    private boolean isSuspiciousActivity(String username, String action, String result) {
        LocalDateTime recentTime = LocalDateTime.now().minus(15, ChronoUnit.MINUTES);
        
        // Múltiplas tentativas de login falhadas
        if ("LOGIN_ATTEMPT".equals(action) && "FAILED".equals(result)) {
            long failedAttempts = auditRepository.countFailedAttempts(username, "LOGIN_ATTEMPT", recentTime);
            return failedAttempts >= 3;
        }
        
        // Tentativas de acesso a recursos não autorizados
        if (action.contains("ACCESS_DENIED")) {
            List<SecurityAuditLog> recentDenials = auditRepository
                .findByUsernameAndTimestampAfter(username, recentTime);
            return recentDenials.stream()
                .filter(log -> log.getAction().contains("ACCESS_DENIED"))
                .count() >= 5;
        }
        
        return false;
    }
    
    private void handleSuspiciousActivity(String username, String action, String details) {
        log.warn("SUSPICIOUS_ACTIVITY_DETECTED: User {} - Action {} - Details: {}", 
            username, action, details);
        
        // Registrar evento de alta prioridade
        SecurityAuditLog suspiciousLog = SecurityAuditLog.builder()
            .username(username)
            .action("SUSPICIOUS_ACTIVITY_DETECTED")
            .timestamp(LocalDateTime.now())
            .success(false)
            .details("Detected suspicious pattern: " + action + " - " + details)
            .riskLevel(SecurityAuditLog.RiskLevel.CRITICAL)
            .build();
        
        auditRepository.save(suspiciousLog);
        
        // Aqui poderia enviar alertas por email, SMS, etc.
        // alertService.sendSecurityAlert(username, action, details);
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // Todo dia às 2h da manhã
    @Transactional
    public void cleanupOldLogs() {
        if (!auditEnabled) return;
        
        try {
            LocalDateTime cutoff = LocalDateTime.now().minus(retentionDays, ChronoUnit.DAYS);
            auditRepository.deleteOldLogs(cutoff);
            log.info("Limpeza de logs de auditoria concluída. Removidos logs anteriores a {}", cutoff);
        } catch (Exception e) {
            log.error("Erro durante limpeza de logs de auditoria", e);
        }
    }
    
    public List<SecurityAuditLog> getRecentSecurityEvents(String username, int hours) {
        LocalDateTime since = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
        return auditRepository.findByUsernameAndTimestampAfter(username, since);
    }
    
    public List<SecurityAuditLog> getFailedLoginAttempts(int hours) {
        LocalDateTime since = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
        return auditRepository.findByActionAndSuccessFalseAndTimestampAfter("LOGIN_ATTEMPT", since);
    }
}