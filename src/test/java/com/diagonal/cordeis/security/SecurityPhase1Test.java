package com.diagonal.cordeis.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "security.password.min-length=8",
    "security.rate-limit.login-attempts=3",
    "security.session.timeout-minutes=30"
})
class SecurityPhase1Test {
    
    @Test
    void testPasswordValidation() {
        PasswordSecurityService passwordService = new PasswordSecurityService();
        
        // Teste senha fraca
        var weakResult = passwordService.validatePassword("123", null);
        assertFalse(weakResult.isValid());
        assertTrue(weakResult.getErrors().size() > 0);
        
        // Teste senha forte
        var strongResult = passwordService.validatePassword("MinhaSenh@123", null);
        assertTrue(strongResult.isValid());
        assertTrue(strongResult.getStrength() > 0.7);
    }
    
    @Test
    void testRateLimiting() {
        RateLimitingService rateLimitService = new RateLimitingService();
        
        String testUser = "testuser";
        
        // Primeiras tentativas devem ser permitidas
        assertFalse(rateLimitService.isRateLimited(testUser, RateLimitingService.ActionType.LOGIN));
        
        // Simular múltiplas tentativas falhadas
        for (int i = 0; i < 5; i++) {
            rateLimitService.recordAttempt(testUser, RateLimitingService.ActionType.LOGIN, false);
        }
        
        // Agora deve estar bloqueado
        assertTrue(rateLimitService.isRateLimited(testUser, RateLimitingService.ActionType.LOGIN));
    }
    
    @Test
    void testPasswordStrengthCalculation() {
        PasswordSecurityService passwordService = new PasswordSecurityService();
        
        // Senha muito fraca
        double weakStrength = passwordService.calculatePasswordStrength("123");
        assertTrue(weakStrength < 0.3);
        
        // Senha média
        double mediumStrength = passwordService.calculatePasswordStrength("password123");
        assertTrue(mediumStrength >= 0.3 && mediumStrength < 0.7);
        
        // Senha forte
        double strongStrength = passwordService.calculatePasswordStrength("MinhaSenh@Forte123!");
        assertTrue(strongStrength >= 0.7);
    }
}