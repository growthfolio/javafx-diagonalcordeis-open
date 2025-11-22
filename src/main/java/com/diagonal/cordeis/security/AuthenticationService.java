package com.diagonal.cordeis.security;

import com.diagonal.cordeis.model.Permission;
import com.diagonal.cordeis.model.Role;
import com.diagonal.cordeis.model.User;
import com.diagonal.cordeis.model.UserProfile;
import com.diagonal.cordeis.repository.UserRepository;
import com.diagonal.cordeis.MainView;
import com.diagonal.cordeis.exceptions.SecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import com.diagonal.cordeis.controller.MainController;
import javafx.scene.Parent;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import com.diagonal.cordeis.view.FXMLViewLoader;
import com.diagonal.cordeis.view.SpringContext;
import com.diagonal.cordeis.view.ModalUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j

@Service
public class AuthenticationService {

    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${security.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EnhancedSessionManager sessionManager;

    @Autowired
    private EnhancedAuditService auditService;
    
    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private FXMLViewLoader fxmlViewLoader;

    private final Map<String, LocalDateTime> lockedAccounts = new HashMap<>();

    private User currentUser;
    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public User authenticate(String username, String password) {
        // Check rate limiting
        if (rateLimitingService.isRateLimited(username, RateLimitingService.ActionType.LOGIN)) {
            int remaining = rateLimitingService.getRemainingAttempts(username, RateLimitingService.ActionType.LOGIN);
            auditService.logSecurityEvent(username, "LOGIN_ATTEMPT", "BLOCKED", "Rate limited - " + remaining + " attempts remaining");
            throw new SecurityException("Muitas tentativas de login. Tente novamente em alguns minutos.");
        }
        
        // Check if account is locked
        if (isAccountLocked(username)) {
            auditService.logSecurityEvent(username, "LOGIN_ATTEMPT", "FAILED", "Account is locked");
            throw new SecurityException("Esta conta está temporariamente bloqueada. Tente novamente mais tarde.");
        }

        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    registerFailedAttempt(username);
                    return new SecurityException("Credenciais inválidas");
                });

            if (!user.isActive()) {
                auditService.logSecurityEvent(username, "LOGIN_ATTEMPT", "FAILED", "Account is inactive");
                throw new SecurityException("Conta inativa. Entre em contato com o administrador.");
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                registerFailedAttempt(username);
                rateLimitingService.recordAttempt(username, RateLimitingService.ActionType.LOGIN, false);
                auditService.logSecurityEvent(username, "LOGIN_ATTEMPT", "FAILED", "Invalid password");
                throw new SecurityException("Credenciais inválidas");
            }

            // Reset login attempts on successful login
            user.setLoginAttempts(0);
            user.setLastLogin(LocalDateTime.now());
            rateLimitingService.recordAttempt(username, RateLimitingService.ActionType.LOGIN, true);

            // Ensure permissions are set
            if (user.getPermissions().isEmpty()) {
                user.getPermissions().addAll(user.getDefaultPermissions());
            }

            // Save updated user first
            User savedUser = userRepository.save(user);

            // Create session after saving user
            String sessionId = sessionManager.createSession(savedUser);
            savedUser.setSessionId(sessionId);
            
            // Update with session ID in separate transaction
            userRepository.save(savedUser);
            
            user = savedUser;

            // Log successful login
            auditService.logSecurityEvent(username, "LOGIN_SUCCESS", "SUCCESS", "User logged in successfully");

            return user;
        } catch (SecurityException e) {
            try {
                rateLimitingService.recordAttempt(username, RateLimitingService.ActionType.LOGIN, false);
            } catch (Exception ignored) {}
            throw e;
        } catch (Exception e) {
            log.error("Authentication error", e);
            try {
                rateLimitingService.recordAttempt(username, RateLimitingService.ActionType.LOGIN, false);
                auditService.logSecurityEvent(username, "LOGIN_ATTEMPT", "ERROR", e.getMessage());
            } catch (Exception ignored) {}
            throw new SecurityException("Erro de autenticação. Tente novamente.");
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.hasRole(Role.ROLE_ADMIN);
    }

    public boolean isAdmin(User user) {
        return user.getPermissions().contains(Permission.MANAGE_BOOKS) &&
               user.getPermissions().contains(Permission.MANAGE_COLLECTIONS) &&
               user.getPermissions().contains(Permission.IMPORT_BOOKS);
    }

    /**
     * Check if the user is authenticated
     * @return true if a user is logged in, false otherwise
     */
    public boolean isAuthenticated() {
        return currentUser != null &&
               sessionManager.isSessionValid(currentUser.getSessionId());
    }

    /**
     * Check if this is public access (no authenticated user)
     * @return true if no user is logged in or user has PUBLIC profile
     */
    public boolean isPublicAccess() {
        return currentUser == null ||
               currentUser.getProfile() == UserProfile.PUBLIC;
    }

    /**
     * Check if user has a specific permission
     * @param permission The permission to check
     * @return true if permission is granted, false otherwise
     */
    public boolean hasPermission(Permission permission) {
        // Public access permissions for printing and viewing books
        if (permission == Permission.VIEW_BOOKS ||
            permission == Permission.VIEW_PRINTERS ||
            permission == Permission.PRINT_BOOKS) {
            return true;
        }

        return isAuthenticated() &&
               (currentUser.hasRole(Role.ROLE_ADMIN) ||
                currentUser.getProfile() == UserProfile.ADMIN ||
                currentUser.hasPermission(permission));
    }

    public void checkPermission(Permission permission) {
        if (!hasPermission(permission)) {
            throw new AccessDeniedException("Access denied: Missing required permission - " + permission);
        }
    }

    public void checkPermissions(Permission... permissions) {
        for (Permission permission : permissions) {
            if (!hasPermission(permission)) {
                throw new AccessDeniedException("Access denied: Missing required permission - " + permission);
            }
        }
    }

    public void loadMainView() {
        try {
            Scene scene = fxmlViewLoader.loadScene("/fxml/main-view.fxml");
            Platform.runLater(() -> {
                primaryStage.setScene(scene);
                primaryStage.show();
            });
        } catch (Exception e) {
            log.error("Erro ao carregar main view", e);
            throw new RuntimeException("Error loading main view", e);
        }
    }

    public void loadLoginView() {
    try {
        if (primaryStage.getScene() == null) {
            Scene scene = fxmlViewLoader.loadScene("/fxml/login-view.fxml");
            Platform.runLater(() -> {
                primaryStage.setScene(scene);
                primaryStage.show();
            });
        } else {
            Platform.runLater(() -> {
                try {
                    var view = fxmlViewLoader.loadWithController("/fxml/login-view.fxml");
                    Parent loginView = view.root();

                    // Usar ModalUtils para dimensionamento automático
                    Stage loginStage = ModalUtils.createAuthModalStage(
                        "Login - Sistema de Cordéis", 
                        loginView, 
                        primaryStage
                    );

                    // Centralizar o modal na tela principal
                    loginStage.setOnShown(e -> {
                        double centerX = primaryStage.getX() + (primaryStage.getWidth() - loginStage.getWidth()) / 2;
                        double centerY = primaryStage.getY() + (primaryStage.getHeight() - loginStage.getHeight()) / 2;
                        loginStage.setX(centerX);
                        loginStage.setY(centerY);
                    });

                    loginStage.showAndWait();

                    if (isAuthenticated()) {
                        MainController mainController = SpringContext.getContext().getBean(MainController.class);
                        if (mainController != null) {
                            mainController.configurarAcessoTabs();
                        }
                    }
                } catch (Exception e) {
                    log.error("Erro ao carregar modal de login", e);
                    throw new RuntimeException("Error loading login modal", e);
                }
            });
        }
    } catch (Exception e) {
        log.error("Erro ao carregar login view", e);
        throw new RuntimeException("Error loading login view", e);
    }
}


    public void loadRegisterView() {
        try {
            // Verificar se é a inicialização inicial da aplicação
            if (primaryStage.getScene() == null) {
                // Primeira execução - carregar diretamente na janela principal
                Scene scene = fxmlViewLoader.loadScene("/fxml/register-view.fxml");
                Platform.runLater(() -> {
                    primaryStage.setScene(scene);
                    primaryStage.show();
                });
            } else {
                // Já existe uma tela aberta - mostrar registro como modal
                Platform.runLater(() -> {
                    try {
                        var view = fxmlViewLoader.loadWithController("/fxml/register-view.fxml");
                        Parent registerView = view.root();

                        // Usar ModalUtils para dimensionamento automático
                        Stage registerStage = ModalUtils.createAuthModalStage(
                            "Cadastro - Sistema de Cordéis", 
                            registerView, 
                            primaryStage
                        );

                        // Centralizar o modal na tela principal
                        registerStage.setOnShown(e -> {
                            double centerX = primaryStage.getX() + (primaryStage.getWidth() - registerStage.getWidth()) / 2;
                            double centerY = primaryStage.getY() + (primaryStage.getHeight() - registerStage.getHeight()) / 2;
                            registerStage.setX(centerX);
                            registerStage.setY(centerY);
                        });

                        // Mostrar o modal e aguardar
                        registerStage.showAndWait();
                    } catch (Exception e) {
                        log.error("Erro ao carregar modal de registro", e);
                        throw new RuntimeException("Error loading register modal", e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Erro ao carregar register view", e);
            throw new RuntimeException("Error loading register view", e);
        }
    }

    public void logout() {
        if (this.currentUser != null) {
            auditService.logSecurityEvent(currentUser.getUsername(), "LOGOUT", "SUCCESS", "User logged out");
            sessionManager.invalidateSession(currentUser.getSessionId());
            this.currentUser = null;

            // Após logout, atualizar a UI para refletir o estado não-autenticado
            Platform.runLater(() -> {

                MainController mainController = SpringContext.getContext().getBean(MainController.class);
                if (mainController != null) {
                    mainController.configurarAcessoTabs();
                }

                // Exibir modal de login
                loadLoginView();
            });
        }
    }

    /**
     * Register a failed login attempt for a username
     * @param username The username that failed to log in
     */
    private void registerFailedAttempt(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            if (user.getLoginAttempts() >= maxLoginAttempts) {
                lockedAccounts.put(username, LocalDateTime.now());
                log.warn("Account locked: {} due to too many failed attempts", username);
                auditService.logSecurityEvent(username, "ACCOUNT_LOCKED", "WARNING",
                        "Account locked after " + maxLoginAttempts + " failed attempts");
            }
            userRepository.save(user);
        });
    }

    /**
     * Check if an account is currently locked
     * @param username The username to check
     * @return true if the account is locked, false otherwise
     */
    private boolean isAccountLocked(String username) {
        if (!lockedAccounts.containsKey(username)) {
            return false;
        }

        LocalDateTime lockTime = lockedAccounts.get(username);
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLock = java.time.temporal.ChronoUnit.MINUTES.between(lockTime, now);

        if (minutesSinceLock >= lockoutDurationMinutes) {
            // Lock duration expired, remove from locked accounts
            lockedAccounts.remove(username);
            log.info("Lock expired for account: {}", username);
            return false;
        }

        return true;
    }
}
