package com.diagonal.cordeis.controller;

import com.diagonal.cordeis.model.Role;
import com.diagonal.cordeis.model.User;
import com.diagonal.cordeis.repository.UserRepository;
import com.diagonal.cordeis.security.AuthenticationService;
import com.diagonal.cordeis.security.PasswordSecurityService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import java.util.HashSet;
import java.util.Set;

@Component
public class RegisterController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ProgressBar passwordStrengthBar;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField nomeField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label successLabel;
    @FXML
    private Label nomeErrorLabel;
    @FXML
    private Label usernameErrorLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label confirmPasswordErrorLabel;
    @FXML
    private Label passwordStrengthLabel;
    
    @Autowired
    private AuthenticationService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordSecurityService passwordSecurityService;
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    private static final int MIN_PASSWORD_LENGTH = 6;
    private PauseTransition usernameValidationDelay;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupValidation();
        setupPasswordStrengthMeter();
        
        usernameValidationDelay = new PauseTransition(Duration.millis(500));
        usernameValidationDelay.setOnFinished(e -> validateUsername());
    }
    
    private void setupValidation() {
        nomeField.textProperty().addListener((obs, old, newValue) -> {
            validateNome(newValue);
        });
        
        usernameField.textProperty().addListener((obs, old, newValue) -> {
            usernameValidationDelay.playFromStart();
        });
        
        passwordField.textProperty().addListener((obs, old, newValue) -> {
            validatePassword(newValue);
            validateConfirmPassword();
        });
        
        confirmPasswordField.textProperty().addListener((obs, old, newValue) -> {
            validateConfirmPassword();
        });
    }
    
    private void setupPasswordStrengthMeter() {
        passwordStrengthBar.progressProperty().addListener((obs, old, newValue) -> {
            // Remover todas as classes de estilo existentes
            passwordStrengthBar.getStyleClass().removeAll(
                "password-strength-weak", "password-strength-medium", "password-strength-strong");

            if (newValue.doubleValue() < 0.3) {
                // Vermelho - Fraca
                passwordStrengthBar.getStyleClass().add("password-strength-weak");
            } else if (newValue.doubleValue() < 0.7) {
                // Amarelo - Média
                passwordStrengthBar.getStyleClass().add("password-strength-medium");
            } else {
                // Verde - Forte
                passwordStrengthBar.getStyleClass().add("password-strength-strong");
            }
        });
    }
    
    private void validateNome(String nome) {
        if (nome.trim().isEmpty()) {
            showFieldError(nomeErrorLabel, "Nome é obrigatório");
        } else if (nome.trim().length() < 3) {
            showFieldError(nomeErrorLabel, "Nome deve ter pelo menos 3 caracteres");
        } else {
            hideFieldError(nomeErrorLabel);
        }
    }
    
    private void validateUsername() {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            showFieldError(usernameErrorLabel, "Nome de usuário é obrigatório");
            return;
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            showFieldError(usernameErrorLabel, "Use apenas letras, números, _ ou - (3-20 caracteres)");
            return;
        }
        
        try {
            // Verificar se o username já existe
            if (userRepository.findByUsername(username).isPresent()) {
                showFieldError(usernameErrorLabel, "Este nome de usuário já está em uso");
                return;
            }
        } catch (Exception e) {
            // Continuar mesmo com erro na verificação
        }
        
        hideFieldError(usernameErrorLabel);
    }
    
    private void validatePassword(String password) {
        if (password.isEmpty()) {
            showFieldError(passwordErrorLabel, "Senha é obrigatória");
            passwordStrengthBar.setProgress(0);
            updatePasswordStrengthLabel(0, null);
            return;
        }
        
        try {
            // Usar o novo serviço de validação de senhas
            PasswordSecurityService.PasswordValidationResult result = 
                passwordSecurityService.validatePassword(password, null);
            
            if (!result.isValid()) {
                showFieldError(passwordErrorLabel, String.join(", ", result.getErrors()));
            } else {
                hideFieldError(passwordErrorLabel);
            }
            
            // Atualizar barra de força
            passwordStrengthBar.setProgress(result.getStrength());
            updatePasswordStrengthLabel(result.getStrength(), result);
            
        } catch (Exception e) {
            // Fallback para validação simples
            if (password.length() < 6) {
                showFieldError(passwordErrorLabel, "Senha deve ter pelo menos 6 caracteres");
            } else {
                hideFieldError(passwordErrorLabel);
            }
            passwordStrengthBar.setProgress(0.5);
            updatePasswordStrengthLabel(0.5, null);
        }
    }
    
    private void validateConfirmPassword() {
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        
        if (confirm.isEmpty()) {
            showFieldError(confirmPasswordErrorLabel, "Confirmação de senha é obrigatória");
        } else if (!password.equals(confirm)) {
            showFieldError(confirmPasswordErrorLabel, "As senhas não conferem");
        } else {
            hideFieldError(confirmPasswordErrorLabel);
        }
    }
    

    
    private void updatePasswordStrengthLabel(double strength, PasswordSecurityService.PasswordValidationResult result) {
        // Remover todas as classes de estilo existentes do label
        passwordStrengthLabel.getStyleClass().removeAll(
            "password-strength-weak", "password-strength-medium", "password-strength-strong");
        
        if (strength == 0) {
            passwordStrengthLabel.setText("");
            passwordStrengthLabel.setVisible(false);
        } else {
            String strengthText = result != null ? result.getStrengthText() : getStrengthText(strength);
            passwordStrengthLabel.setText("Senha " + strengthText.toLowerCase());
            
            if (strength < 0.3) {
                passwordStrengthLabel.getStyleClass().add("password-strength-weak");
            } else if (strength < 0.6) {
                passwordStrengthLabel.getStyleClass().add("password-strength-medium");
            } else {
                passwordStrengthLabel.getStyleClass().add("password-strength-strong");
            }
            
            passwordStrengthLabel.setVisible(true);
        }
    }
    
    private String getStrengthText(double strength) {
        if (strength < 0.3) return "Fraca";
        if (strength < 0.6) return "Média";
        if (strength < 0.8) return "Boa";
        return "Forte";
    }
    
    private void showFieldError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
    }
    
    private void hideFieldError(Label label) {
        label.setVisible(false);
    }
    
    @FXML
    private void handleRegister() {
        clearGlobalMessages();
        
        // Validar todos os campos
        String nome = nomeField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Realizar todas as validações novamente
        validateNome(nome);
        validateUsername();
        validatePassword(password);
        validateConfirmPassword();
        
        // Verificar se há erros
        if (hasErrors()) {
            showError("Por favor, corrija os erros antes de continuar.");
            return;
        }
        
        try {
            // Determinar se este será o primeiro usuário (administrador)
            boolean isFirstUser = userRepository.count() == 0;
            
            // Criar o usuário
            Set<Role> roles = new HashSet<>();
            roles.add(isFirstUser ? Role.ROLE_ADMIN : Role.ROLE_USER);
            
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setNome(nome);
            user.setRoles(roles);
            user.setActive(true);
            
            userRepository.save(user);
            
            // Mostrar mensagem de sucesso
            String successMessage = isFirstUser 
                ? "Usuário administrador criado com sucesso!" 
                : "Usuário criado com sucesso!";
            
            showSuccess(successMessage);
            clearFields();
            
            // Redirecionar para login após 2 segundos
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> {
                // Fechar modal de cadastro atual
                Stage currentStage = (Stage) nomeField.getScene().getWindow();
                currentStage.close();
                // Abrir modal de login
                authService.loadLoginView();
            });
            pause.play();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao cadastrar usuário: " + e.getMessage());
        }
    }
    
    private boolean hasErrors() {
        return nomeErrorLabel.isVisible() ||
               usernameErrorLabel.isVisible() ||
               passwordErrorLabel.isVisible() ||
               confirmPasswordErrorLabel.isVisible();
    }
    
    @FXML
    private void handleBack() {
        // Fechar janela de registro atual
        Stage currentStage = (Stage) nomeField.getScene().getWindow();
        currentStage.close();

        // Abrir janela de login
        authService.loadLoginView();
    }
    
    private void clearGlobalMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        
        // Tentar encontrar e mostrar o container de erro
        try {
            var errorContainer = errorLabel.getParent().lookup("#errorContainer");
            if (errorContainer != null) {
                errorContainer.setVisible(true);
                errorContainer.setManaged(true);
            }
        } catch (Exception ignored) {}
        
        // Auto-hide após 5 segundos
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        });
        pause.play();
    }
    
    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
        
        // Tentar encontrar e mostrar o container de sucesso
        try {
            var successContainer = successLabel.getParent().lookup("#successContainer");
            if (successContainer != null) {
                successContainer.setVisible(true);
                successContainer.setManaged(true);
            }
        } catch (Exception ignored) {}
    }
    
    private void clearFields() {
        nomeField.clear();
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        passwordStrengthBar.setProgress(0);
    }

    @FXML
    private void handleCloseWindow() {
        // Obter a janela atual e fechá-la
        Stage stage = (Stage) nomeField.getScene().getWindow();
        stage.close();
    }
}
