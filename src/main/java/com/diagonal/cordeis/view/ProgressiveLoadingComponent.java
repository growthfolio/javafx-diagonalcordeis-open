package com.diagonal.cordeis.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Componente para carregamento progressivo e otimização de performance percebida
 * Implementa lazy loading, virtual flow e preload inteligente
 */
public class ProgressiveLoadingComponent<T> extends StackPane {
    
    private final ListView<T> listView;
    private final ProgressIndicator progressIndicator;
    private final Label statusLabel;
    private final VBox loadingContainer;
    private final ObservableList<T> items = FXCollections.observableArrayList();
    
    // Configurações de carregamento
    private int pageSize = 50;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private boolean enableVirtualFlow = true;
    
    // Callbacks
    private Supplier<CompletableFuture<List<T>>> dataProvider;
    private Consumer<T> onItemSelected;
    private Consumer<List<T>> onDataLoaded;
    
    public ProgressiveLoadingComponent() {
        this.listView = new ListView<>(items);
        this.progressIndicator = new ProgressIndicator();
        this.statusLabel = new Label();
        this.loadingContainer = new VBox(10);
        
        setupComponent();
    }
    
    private void setupComponent() {
        // Configurar ListView
        listView.getStyleClass().add("progressive-list");
        if (enableVirtualFlow) {
            listView.getStyleClass().add("virtual-flow-enabled");
        }
        
        // Configurar loading container
        loadingContainer.setAlignment(Pos.CENTER);
        loadingContainer.getStyleClass().add("progress-container");
        loadingContainer.setVisible(false);
        
        progressIndicator.getStyleClass().add("progress-spinner");
        progressIndicator.setPrefSize(40, 40);
        
        statusLabel.getStyleClass().add("progress-status");
        statusLabel.setText("Carregando dados...");
        
        loadingContainer.getChildren().addAll(progressIndicator, statusLabel);
        
        // Adicionar componentes ao StackPane
        getChildren().addAll(listView, loadingContainer);
        
        // Configurar scroll listener para lazy loading
        setupScrollListener();
        
        // Configurar seleção
        setupSelectionListener();
    }
    
    private void setupScrollListener() {
        listView.setOnScroll(event -> {
            // Verificar se está próximo do final da lista
            if (isNearBottom() && !isLoading && hasMoreData) {
                loadNextPage();
            }
        });
        
        // Alternativa para detecção de scroll
        listView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    ScrollBar scrollBar = getVerticalScrollBar();
                    if (scrollBar != null) {
                        scrollBar.valueProperty().addListener((obs2, oldVal, newVal) -> {
                            if (newVal.doubleValue() > 0.9 && !isLoading && hasMoreData) {
                                loadNextPage();
                            }
                        });
                    }
                });
            }
        });
    }
    
    private void setupSelectionListener() {
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && onItemSelected != null) {
                onItemSelected.accept(newSelection);
            }
        });
    }
    
    private ScrollBar getVerticalScrollBar() {
        for (Node node : listView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar) {
                if (scrollBar.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                    return scrollBar;
                }
            }
        }
        return null;
    }
    
    private boolean isNearBottom() {
        int visibleItems = (int) (listView.getHeight() / 24); // Estimativa baseada na altura do item
        int totalItems = items.size();
        int lastVisibleIndex = listView.getSelectionModel().getSelectedIndex() + visibleItems;
        
        return lastVisibleIndex >= totalItems - 5; // Carregar quando faltam 5 itens para o final
    }
    
    /**
     * Inicia o carregamento da primeira página
     */
    public void loadInitialData() {
        if (dataProvider == null) {
            return;
        }
        
        currentPage = 0;
        items.clear();
        hasMoreData = true;
        loadNextPage();
    }
    
    /**
     * Carrega a próxima página de dados
     */
    private void loadNextPage() {
        if (isLoading || !hasMoreData || dataProvider == null) {
            return;
        }
        
        isLoading = true;
        showLoading(true);
        
        // Criar task para carregamento em background
        Task<List<T>> loadTask = new Task<>() {
            @Override
            protected List<T> call() throws Exception {
                return dataProvider.get().get();
            }
            
            @Override
            protected void succeeded() {
                List<T> newData = getValue();
                Platform.runLater(() -> {
                    processLoadedData(newData);
                    isLoading = false;
                    showLoading(false);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    handleLoadError(getException());
                    isLoading = false;
                    showLoading(false);
                });
            }
        };
        
        // Executar task em background
        new Thread(loadTask).start();
    }
    
    private void processLoadedData(List<T> newData) {
        if (newData == null || newData.isEmpty()) {
            hasMoreData = false;
            statusLabel.setText("Todos os dados foram carregados");
            return;
        }
        
        // Aplicar animação de entrada para novos itens
        int startIndex = items.size();
        items.addAll(newData);
        
        // Animar novos itens
        animateNewItems(startIndex, newData.size());
        
        currentPage++;
        
        if (newData.size() < pageSize) {
            hasMoreData = false;
        }
        
        // Callback para dados carregados
        if (onDataLoaded != null) {
            onDataLoaded.accept(newData);
        }
        
        // Mostrar estatísticas
        updateStatusLabel();
    }
    
    private void animateNewItems(int startIndex, int count) {
        Platform.runLater(() -> {
            for (int i = 0; i < count; i++) {
                final int index = startIndex + i;
                final int delay = i;
                if (index < items.size()) {
                    // Para cada célula visível, aplicar fade-in
                    Task<Void> animationTask = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            Thread.sleep(delay * 50); // Delay escalonado
                            return null;
                        }
                        
                        @Override
                        protected void succeeded() {
                            Platform.runLater(() -> {
                                // Aplicar animação ao item visível
                                if (listView.lookupAll(".list-cell").size() > index) {
                                    Node cell = (Node) listView.lookupAll(".list-cell").toArray()[index];
                                    if (cell != null) {
                                        UXUtils.slideUp(cell, Duration.millis(300));
                                    }
                                }
                            });
                        }
                    };
                    
                    new Thread(animationTask).start();
                }
            }
        });
    }
    
    private void handleLoadError(Throwable error) {
        statusLabel.setText("Erro ao carregar dados: " + error.getMessage());
        statusLabel.getStyleClass().add("error-text");
        
        // Mostrar toast de erro
        UXUtils.showToast(this, "Erro ao carregar dados", UXUtils.ToastType.ERROR, Duration.seconds(5));
    }
    
    private void showLoading(boolean show) {
        loadingContainer.setVisible(show);
        if (show) {
            statusLabel.setText("Carregando página " + (currentPage + 1) + "...");
            UXUtils.fadeIn(loadingContainer, Duration.millis(200));
        } else {
            UXUtils.fadeOut(loadingContainer, Duration.millis(200), null);
        }
    }
    
    private void updateStatusLabel() {
        int totalItems = items.size();
        String status;
        
        if (hasMoreData) {
            status = String.format("%d itens carregados • Carregando mais...", totalItems);
        } else {
            status = String.format("Total: %d itens", totalItems);
        }
        
        statusLabel.setText(status);
        statusLabel.getStyleClass().remove("error-text");
    }
    
    /**
     * Adiciona preload inteligente baseado na interação do usuário
     */
    public void enableSmartPreload() {
        // Preload quando o usuário para de interagir por um tempo
        Timeline preloadTimer = new Timeline();
        preloadTimer.getKeyFrames().add(new KeyFrame(Duration.seconds(2), e -> {
            if (!isLoading && hasMoreData && isNearBottom()) {
                loadNextPage();
            }
        }));
        
        // Reset timer a cada interação
        listView.setOnMouseMoved(e -> {
            preloadTimer.stop();
            preloadTimer.play();
        });
        
        listView.setOnKeyPressed(e -> {
            preloadTimer.stop();
            preloadTimer.play();
        });
    }
    
    /**
     * Força atualização dos dados
     */
    public void refresh() {
        items.clear();
        currentPage = 0;
        hasMoreData = true;
        loadInitialData();
    }
    
    // Getters e Setters
    public void setDataProvider(Supplier<CompletableFuture<List<T>>> dataProvider) {
        this.dataProvider = dataProvider;
    }
    
    public void setOnItemSelected(Consumer<T> onItemSelected) {
        this.onItemSelected = onItemSelected;
    }
    
    public void setOnDataLoaded(Consumer<List<T>> onDataLoaded) {
        this.onDataLoaded = onDataLoaded;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public void setCellFactory(javafx.util.Callback<ListView<T>, ListCell<T>> cellFactory) {
        listView.setCellFactory(cellFactory);
    }
    
    public ListView<T> getListView() {
        return listView;
    }
    
    public ObservableList<T> getItems() {
        return items;
    }
    
    public boolean isLoading() {
        return isLoading;
    }
    
    public void setEnableVirtualFlow(boolean enableVirtualFlow) {
        this.enableVirtualFlow = enableVirtualFlow;
        if (enableVirtualFlow) {
            listView.getStyleClass().add("virtual-flow-enabled");
        } else {
            listView.getStyleClass().remove("virtual-flow-enabled");
        }
    }
}
