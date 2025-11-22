package com.diagonal.cordeis.security;

import com.diagonal.cordeis.model.User;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PasswordSecurityService {
    
    @Value("${security.password.min-length:8}")
    private int minPasswordLength;
    
    @Value("${security.password.require-uppercase:true}")
    private boolean requireUppercase;
    
    @Value("${security.password.require-lowercase:true}")
    private boolean requireLowercase;
    
    @Value("${security.password.require-digits:true}")
    private boolean requireDigits;
    
    @Value("${security.password.require-special-chars:true}")
    private boolean requireSpecialChars;
    
    @Value("${security.password.history-size:5}")
    private int passwordHistorySize;
    
    // Lista das 50 senhas mais comuns
    private static final Set<String> COMMON_PASSWORDS = Set.of(
        "123456", "password", "123456789", "12345678", "12345", "1234567", "1234567890",
        "qwerty", "abc123", "111111", "123123", "admin", "letmein", "welcome", "monkey",
        "password123", "dragon", "master", "hello", "login", "princess", "solo", "qwerty123",
        "starwars", "121212", "freedom", "superman", "michael", "jordan23", "harley",
        "robert", "matthew", "daniel", "andrew", "joshua", "anthony", "william", "david",
        "charles", "thomas", "christopher", "joseph", "jessica", "ashley", "amanda", "melissa",
        "sarah", "michelle", "kimberly", "amy", "angela"
    );
    
    public PasswordValidationResult validatePassword(String password, User user) {
        PasswordValidationResult result = new PasswordValidationResult();
        
        if (password == null || password.isEmpty()) {
            result.addError("Senha é obrigatória");
            return result;
        }
        
        // Verificar comprimento mínimo
        if (password.length() < minPasswordLength) {
            result.addError("Senha deve ter pelo menos " + minPasswordLength + " caracteres");
        }
        
        // Verificar complexidade
        if (requireUppercase && !hasUpperCase(password)) {
            result.addError("Deve conter ao menos uma letra maiúscula");
        }
        
        if (requireLowercase && !hasLowerCase(password)) {
            result.addError("Deve conter ao menos uma letra minúscula");
        }
        
        if (requireDigits && !hasDigit(password)) {
            result.addError("Deve conter ao menos um número");
        }
        
        if (requireSpecialChars && !hasSpecialChar(password)) {
            result.addError("Deve conter ao menos um caractere especial (!@#$%^&*)");
        }
        
        // Verificar senhas comuns
        if (isCommonPassword(password)) {
            result.addError("Esta senha é muito comum. Escolha uma senha mais segura");
        }
        
        // Calcular força da senha
        result.setStrength(calculatePasswordStrength(password));
        
        return result;
    }
    
    public double calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0.0;
        
        int score = 0;
        int maxScore = 10;
        
        // Comprimento (0-3 pontos)
        if (password.length() >= minPasswordLength) score++;
        if (password.length() >= 10) score++;
        if (password.length() >= 12) score++;
        
        // Complexidade (0-4 pontos)
        if (hasUpperCase(password)) score++;
        if (hasLowerCase(password)) score++;
        if (hasDigit(password)) score++;
        if (hasSpecialChar(password)) score++;
        
        // Diversidade de caracteres (0-2 pontos)
        if (password.chars().distinct().count() >= password.length() * 0.7) score++;
        if (!hasRepeatingPatterns(password)) score++;
        
        // Penalizar senhas comuns
        if (isCommonPassword(password)) score = Math.max(0, score - 3);
        
        return Math.min(1.0, (double) score / maxScore);
    }
    
    private boolean hasUpperCase(String password) {
        return Pattern.compile("[A-Z]").matcher(password).find();
    }
    
    private boolean hasLowerCase(String password) {
        return Pattern.compile("[a-z]").matcher(password).find();
    }
    
    private boolean hasDigit(String password) {
        return Pattern.compile("[0-9]").matcher(password).find();
    }
    
    private boolean hasSpecialChar(String password) {
        return Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]").matcher(password).find();
    }
    
    private boolean isCommonPassword(String password) {
        return COMMON_PASSWORDS.contains(password.toLowerCase());
    }
    
    private boolean hasRepeatingPatterns(String password) {
        // Verificar padrões repetitivos simples como "aaa", "123", "abc"
        for (int i = 0; i < password.length() - 2; i++) {
            String substr = password.substring(i, i + 3);
            if (substr.equals("123") || substr.equals("abc") || 
                substr.charAt(0) == substr.charAt(1) && substr.charAt(1) == substr.charAt(2)) {
                return true;
            }
        }
        return false;
    }
    
    @Data
    public static class PasswordValidationResult {
        private List<String> errors = new ArrayList<>();
        private double strength = 0.0;
        private boolean valid = true;
        
        public void addError(String error) {
            errors.add(error);
            valid = false;
        }
        
        public String getStrengthText() {
            if (strength < 0.3) return "Fraca";
            if (strength < 0.6) return "Média";
            if (strength < 0.8) return "Boa";
            return "Forte";
        }
        
        public String getStrengthColor() {
            if (strength < 0.3) return "#f44336"; // Vermelho
            if (strength < 0.6) return "#ff9800"; // Laranja
            if (strength < 0.8) return "#2196f3"; // Azul
            return "#4caf50"; // Verde
        }
    }
}