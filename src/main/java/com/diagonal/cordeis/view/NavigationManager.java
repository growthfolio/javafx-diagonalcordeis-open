package com.diagonal.cordeis.view;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Gerenciador de navegação com transições suaves, cache de estado e breadcrumbs
 */
@Slf4j
public class NavigationManager {
    
    private final TabPane tabPane;
    private final Map<String, Node> contentCache = new HashMap<>();
    private final Map<String, String> tabTitles = new HashMap<>();
    private final Map<String, Long> tabCounters = new HashMap<>();
    private StackPane breadcrumbContainer;
    private Consumer<String> breadcrumbClickHandler;
    
    public NavigationManager(TabPane tabPane) {
        this.tabPane = tabPane;
        setupNavigationListeners();
    }
    
    /**
     * Configura os listeners de navegação para transições suaves
     */
    private void setupNavigationListeners() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == null) return;
            
            // Aplicar transição suave
            if (newTab.getContent() != null) {
                UXUtils.fadeIn(newTab.getContent(), Duration.millis(250));
            }
            
            // Atualizar breadcrumb
            updateBreadcrumb(newTab);
            
            log.debug("Navegação para tab: {}", newTab.getId());
        });
    }
    
    /**
     * Adiciona conteúdo ao cache para navegação rápida
     */
    public void cacheContent(String tabId, Node content) {
        contentCache.put(tabId, content);
        log.debug("Conteúdo cacheado para tab: {}", tabId);
    }
    
    /**
     * Recupera conteúdo do cache
     */
    public Node getCachedContent(String tabId) {
        return contentCache.get(tabId);
    }
    
    /**
     * Define o título de uma tab com contador opcional
     */
    public void setTabTitle(String tabId, String title, Long counter) {
        tabTitles.put(tabId, title);
        if (counter != null) {
            tabCounters.put(tabId, counter);
        }
        
        Platform.runLater(() -> updateTabDisplay(tabId));
    }
    
    /**
     * Atualiza apenas o contador de uma tab
     */
    public void updateTabCounter(String tabId, long counter) {
        tabCounters.put(tabId, counter);
        Platform.runLater(() -> updateTabDisplay(tabId));
    }
    
    /**
     * Atualiza a exibição de uma tab com título e contador
     */
    private void updateTabDisplay(String tabId) {
        Tab tab = findTabById(tabId);
        if (tab == null) return;
        
        // Implementar lógica de atualização do badge aqui
        // Semelhante ao que já existe no MainController
        log.debug("Atualizando display da tab: {}", tabId);
    }
    
    /**
     * Encontra uma tab pelo ID
     */
    private Tab findTabById(String tabId) {
        return tabPane.getTabs().stream()
            .filter(tab -> tabId.equals(tab.getId()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Configura o breadcrumb container
     */
    public void setBreadcrumbContainer(StackPane container, Consumer<String> clickHandler) {
        this.breadcrumbContainer = container;
        this.breadcrumbClickHandler = clickHandler;
    }
    
    /**
     * Atualiza o breadcrumb baseado na tab atual
     */
    private void updateBreadcrumb(Tab currentTab) {
        if (breadcrumbContainer == null) return;
        
        String tabId = currentTab.getId();
        String[] breadcrumbPath = getBreadcrumbPath(tabId);
        
        Platform.runLater(() -> {
            breadcrumbContainer.getChildren().clear();
            
            if (breadcrumbPath.length > 0) {
                var breadcrumb = UXUtils.createBreadcrumb(breadcrumbClickHandler, breadcrumbPath);
                UXUtils.fadeIn(breadcrumb, Duration.millis(200));
                breadcrumbContainer.getChildren().add(breadcrumb);
            }
        });
    }
    
    /**
     * Define o caminho do breadcrumb para cada tab
     */
    private String[] getBreadcrumbPath(String tabId) {
        return switch (tabId) {
            case "tabImpressao" -> new String[]{"Início", "Impressão"};
            case "tabLivros" -> new String[]{"Início", "Gerenciamento", "Livros"};
            case "tabColecoes" -> new String[]{"Início", "Gerenciamento", "Coleções"};
            case "tabImportar" -> new String[]{"Início", "Ferramentas", "Importar PDFs"};
            default -> new String[]{"Início"};
        };
    }
    
    /**
     * Navega para uma tab específica com animação
     */
    public void navigateToTab(String tabId) {
        Tab targetTab = findTabById(tabId);
        if (targetTab != null) {
            // Aplicar animação de transição se necessário
            Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
            
            if (currentTab != null && currentTab != targetTab) {
                // Fade out da tab atual
                if (currentTab.getContent() != null) {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(150), currentTab.getContent());
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.8);
                    fadeOut.setOnFinished(e -> {
                        tabPane.getSelectionModel().select(targetTab);
                    });
                    fadeOut.play();
                } else {
                    tabPane.getSelectionModel().select(targetTab);
                }
            } else {
                tabPane.getSelectionModel().select(targetTab);
            }
        }
    }
    
    /**
     * Limpa o cache de conteúdo
     */
    public void clearCache() {
        contentCache.clear();
        log.debug("Cache de navegação limpo");
    }
    
    /**
     * Obtém estatísticas do cache
     */
    public String getCacheStats() {
        return String.format("Cache: %d tabs carregadas", contentCache.size());
    }
    
    /**
     * Pré-carrega conteúdo de tabs importantes
     */
    public void preloadImportantTabs(String... tabIds) {
        for (String tabId : tabIds) {
            if (!contentCache.containsKey(tabId)) {
                // Aqui você pode adicionar lógica para pré-carregar conteúdo
                log.debug("Pré-carregamento agendado para tab: {}", tabId);
            }
        }
    }
}
