package com.diagonal.cordeis.controller;

import com.diagonal.cordeis.security.AuthenticationService;
import com.diagonal.cordeis.model.User;
import com.diagonal.cordeis.security.RateLimitingService;
import com.diagonal.cordeis.view.Notificacao;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

@Component
public class LoginController implements Initializable {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private CheckBox rememberMeCheckbox;
    
    @Autowired
    private AuthenticationService authService;
    
    @Autowired
    private RateLimitingService rateLimitingService;
    
    private final Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
    private static final String PREF_USERNAME = "lastUsername";
    private static final String PREF_REMEMBER = "rememberMe";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupInputValidation();
        loadSavedPreferences();
        setupKeyboardShortcuts();
    }
    
    private void setupInputValidation() {
        usernameField.textProperty().addListener((obs, old, newValue) -> {
            validateForm();
        });
        
        passwordField.textProperty().addListener((obs, old, newValue) -> {
            validateForm();
        });
    }
    
    private void loadSavedPreferences() {
        boolean rememberMe = prefs.getBoolean(PREF_REMEMBER, false);
        rememberMeCheckbox.setSelected(rememberMe);
        
        if (rememberMe) {
            String savedUsername = prefs.get(PREF_USERNAME, "");
            usernameField.setText(savedUsername);
            Platform.runLater(() -> passwordField.requestFocus());
        }
    }
    
    private void setupKeyboardShortcuts() {
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }
    
    private void validateForm() {
        boolean isValid = !usernameField.getText().trim().isEmpty() && 
                         !passwordField.getText().trim().isEmpty();
        errorLabel.setVisible(false);
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor, preencha todos os campos");
            return;
        }
        
        // Verificar rate limiting antes de tentar autenticar
        if (rateLimitingService.isRateLimited(username, RateLimitingService.ActionType.LOGIN)) {
            int remaining = rateLimitingService.getRemainingAttempts(username, RateLimitingService.ActionType.LOGIN);
            LocalDateTime lockExpiry = rateLimitingService.getLockoutExpiry(username);
            if (lockExpiry != null) {
                showError("Conta temporariamente bloqueada. Tente novamente após " + 
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(lockExpiry));
            } else {
                showError("Muitas tentativas. Restam " + remaining + " tentativas.");
            }
            return;
        }
        
        try {
            User user = authService.authenticate(username, password);
            if (user != null) {
                savePreferences();
                authService.setCurrentUser(user);
                // Fechar a janela de login modal
                ((Stage) usernameField.getScene().getWindow()).close();
            }
        } catch (com.diagonal.cordeis.exceptions.SecurityException e) {
            // Exibir mensagem específica de erro de segurança com informações de rate limiting
            String errorMessage = e.getMessage();
            int remaining = rateLimitingService.getRemainingAttempts(username, RateLimitingService.ActionType.LOGIN);
            if (remaining > 0 && remaining < 5) {
                errorMessage += " (Restam " + remaining + " tentativas)";
            }
            showError(errorMessage);
            passwordField.clear();
            passwordField.requestFocus();
        } catch (Exception e) {
            Notificacao.erro("Erro durante autenticação" + e);
            showError("Erro ao realizar login. Tente novamente.");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }
    
    private void savePreferences() {
        if (rememberMeCheckbox.isSelected()) {
            prefs.put(PREF_USERNAME, usernameField.getText().trim());
        } else {
            prefs.remove(PREF_USERNAME);
        }
        prefs.putBoolean(PREF_REMEMBER, rememberMeCheckbox.isSelected());
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        
        // Auto-hide error after 5 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> errorLabel.setVisible(false));
        pause.play();
    }
    
    @FXML
    private void handleRegisterLink() {
        // Fechar janela de login atual
        Stage currentStage = (Stage) usernameField.getScene().getWindow();
        currentStage.close();

        // Abrir janela de registro
        authService.loadRegisterView();
    }
    
    @FXML
    private void handleForgotPassword() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showError("Digite seu nome de usuário para recuperar a senha");
            usernameField.requestFocus();
            return;
        }
        
        // TODO: Implementar recuperação de senha
        showError("Funcionalidade de recuperação de senha em desenvolvimento");
    }
    
    @FXML
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }
    
    public User getLoggedUser() {
        return authService.getCurrentUser();
    }

    @FXML
    private void handleCloseWindow() {
        // Obter a janela atual e fechá-la
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}
