package com.diagonal.cordeis.controller;

import com.diagonal.cordeis.model.Permission;
import com.diagonal.cordeis.security.AuthenticationService;
import com.diagonal.cordeis.view.NavigationManager;
import com.diagonal.cordeis.view.Notificacao;
import com.diagonal.cordeis.view.FXMLViewLoader;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MainController {

    @FXML private TabPane tabPane;
    @FXML private Tab tabImpressao;
    @FXML private Tab tabLivros;
    @FXML private Tab tabColecoes;
    @FXML private Tab tabImportar;
    @FXML private StackPane breadcrumbContainer;
    @FXML private Label userNameLabel;
    @FXML private Button loginButton;

    private final AuthenticationService authService;

    private NavigationManager navigationManager;
    private final Map<String, Parent> tabContentCache = new HashMap<>();

    @Autowired
    private FXMLViewLoader fxmlViewLoader;

    @Autowired
    public MainController(AuthenticationService authService) {
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando MainController com controle de acesso configurado");

        // Configurar componentes UX
        setupUXComponents();

        // Configurar transições suaves entre abas
        configurarTransicoesAbas();

        // Configurar acesso às abas baseado no usuário atual
        configurarAcessoTabs();

        // Garantir que o conteúdo da aba impressão seja carregado
        carregarConteudo(tabImpressao, "/fxml/impressao-view.fxml");

        log.info("Interface inicializada baseada no perfil de acesso");
    }

    private void configurarTransicoesAbas() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == null) return;

            String tabId = newTab.getId();
            String fxmlPath = switch (tabId) {
                case "tabImpressao" -> "/fxml/impressao-view.fxml";
                case "tabLivros" -> "/fxml/livro-view.fxml";
                case "tabColecoes" -> "/fxml/colecao-view.fxml";
                case "tabImportar" -> "/fxml/importar-pdfs-view.fxml";
                default -> null;
            };

            if (fxmlPath != null) {
                carregarConteudo(newTab, fxmlPath);
            }
        });
    }

    private void setupUXComponents() {
        // Inicializar gerenciador de navegação
        navigationManager = new NavigationManager(tabPane);

        // Configurar breadcrumb se disponível
        if (breadcrumbContainer != null) {
            navigationManager.setBreadcrumbContainer(breadcrumbContainer, this::handleBreadcrumbClick);
        }

        // Pré-carregar tabs importantes
        navigationManager.preloadImportantTabs("tabImpressao", "tabLivros");
    }

    private void handleBreadcrumbClick(String breadcrumbItem) {
        // Handle breadcrumb navigation
        switch (breadcrumbItem) {
            case "Impressão":
                if (tabImpressao != null) {
                    tabPane.getSelectionModel().select(tabImpressao);
                }
                break;
            case "Livros":
                if (tabLivros != null && authService.hasPermission(Permission.VIEW_BOOKS)) {
                    tabPane.getSelectionModel().select(tabLivros);
                }
                break;
            case "Coleções":
                if (tabColecoes != null && authService.hasPermission(Permission.VIEW_COLLECTIONS)) {
                    tabPane.getSelectionModel().select(tabColecoes);
                }
                break;
            case "Importar":
                if (tabImportar != null && authService.hasPermission(Permission.IMPORT_BOOKS)) {
                    tabPane.getSelectionModel().select(tabImportar);
                }
                break;
        }
    }

    private void carregarConteudo(Tab tab, String fxmlPath) {
        try {
            if (!tabContentCache.containsKey(tab.getId())) {
                Parent content = fxmlViewLoader.load(fxmlPath);
                configureCrudButtonVisibility(content);
                tabContentCache.put(tab.getId(), content);
            }
            Parent content = tabContentCache.get(tab.getId());
            tab.setContent(content);
            aplicarTransicaoSuave(content);
        } catch (Exception e) {
            log.error("Erro ao carregar conteúdo da aba: " + tab.getId(), e);
            Notificacao.erro("Erro ao carregar conteúdo: " + e.getMessage());
        }
    }

    private void configureCrudButtonVisibility(Parent content) {
        // Hide CRUD buttons if user doesn't have manage permissions
        content.lookupAll(".crud-button").forEach(node -> {
            if (node instanceof javafx.scene.control.Button button) {
                boolean isVisible = switch (button.getId()) {
                    case "btnNovoLivro", "btnEditarLivro", "btnExcluirLivro" ->
                        authService.hasPermission(Permission.MANAGE_BOOKS);
                    case "btnNovaColecao", "btnEditarColecao", "btnExcluirColecao" ->
                        authService.hasPermission(Permission.MANAGE_COLLECTIONS);
                    case "btnImportar" ->
                        authService.hasPermission(Permission.IMPORT_BOOKS);
                    default -> true;
                };
                button.setVisible(isVisible);
                button.setManaged(isVisible); // Prevent empty space
            }
        });
    }

    private void aplicarTransicaoSuave(Parent content) {
        FadeTransition fade = new FadeTransition(javafx.util.Duration.millis(150), content);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    @FXML
    public void logout() {
        authService.logout();
    }

    public void configurarAcessoTabs() {
        // Limpar abas existentes exceto impressão
        tabPane.getTabs().clear();
        tabPane.getTabs().add(tabImpressao);

        // Aba de impressão sempre disponível (acesso público)
        tabImpressao.setDisable(false);

        if (authService.isAuthenticated()) {
            // Configurar interface baseada no usuário logado
            userNameLabel.setText(authService.getCurrentUser().getNome());
            loginButton.setText("Sair");
            
            // Configurar ícone do botão para logout
            FontAwesomeIconView logoutIcon = new FontAwesomeIconView();
            logoutIcon.setGlyphName("SIGN_OUT");
            logoutIcon.setSize("16");
            logoutIcon.setFill(javafx.scene.paint.Color.WHITE);
            HBox logoutContainer = new HBox(logoutIcon);
            logoutContainer.setAlignment(javafx.geometry.Pos.CENTER);
            loginButton.setGraphic(logoutContainer);
            
            loginButton.setOnAction(event -> authService.logout());

            // Adicionar abas baseadas em permissão
            if (authService.hasPermission(Permission.VIEW_BOOKS)) {
                tabPane.getTabs().add(tabLivros);
            }

            if (authService.hasPermission(Permission.VIEW_COLLECTIONS)) {
                tabPane.getTabs().add(tabColecoes);
            }

            if (authService.hasPermission(Permission.IMPORT_BOOKS)) {
                tabPane.getTabs().add(tabImportar);
            }
        } else {
            // Interface para usuário não autenticado
            userNameLabel.setText("Visitante");
            loginButton.setText("Entrar");
            
            // Configurar ícone do botão para login
            FontAwesomeIconView loginIcon = new FontAwesomeIconView();
            loginIcon.setGlyphName("SIGN_IN");
            loginIcon.setSize("16");
            loginIcon.setFill(javafx.scene.paint.Color.WHITE);
            HBox loginContainer = new HBox(loginIcon);
            loginContainer.setAlignment(javafx.geometry.Pos.CENTER);
            loginButton.setGraphic(loginContainer);
            
            loginButton.setOnAction(event -> authService.loadLoginView());
        }
    }

    @FXML
    private void handleLogin() {
        // Delegate login modal to AuthenticationService
        authService.loadLoginView();
    }
}
