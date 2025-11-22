package com.diagonal.cordeis.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.List;
import java.util.ArrayList;

/**
 * Componente de pesquisa avançada unificada para o sistema
 * Permite busca global com filtros dinâmicos e histórico
 */
public class AdvancedSearchComponent<T> extends VBox {
    
    private final TextField searchField;
    private final ComboBox<String> searchHistory;
    private final HBox filtersContainer;
    private final Label resultsLabel;
    private final ObservableList<T> sourceList;
    private final FilteredList<T> filteredList;
    private final Map<String, Predicate<T>> activeFilters = new HashMap<>();
    private final ObservableList<String> searchHistoryList = FXCollections.observableArrayList();
    
    private SearchProvider<T> searchProvider;
    private final List<FilterDefinition<T>> availableFilters = new ArrayList<>();
    
    // Configurações
    private int maxHistoryItems = 10;
    private boolean showResultsCount = true;
    private String searchPrompt = "Pesquisar em todos os itens...";
    
    public AdvancedSearchComponent(ObservableList<T> sourceList) {
        this.sourceList = sourceList;
        this.filteredList = new FilteredList<>(sourceList);
        
        this.searchField = new TextField();
        this.searchHistory = new ComboBox<>(searchHistoryList);
        this.filtersContainer = new HBox(8);
        this.resultsLabel = new Label();
        
        setupComponents();
        setupListeners();
        setupStyles();
    }
    
    private void setupComponents() {
        // Campo de pesquisa principal
        searchField.setPromptText(searchPrompt);
        searchField.getStyleClass().add("advanced-search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        // Ícone de pesquisa
        FontAwesomeIconView searchIcon = new FontAwesomeIconView(FontAwesomeIcon.SEARCH);
        searchIcon.setSize("14");
        searchIcon.getStyleClass().add("search-icon");
        
        // Botão de limpar pesquisa
        Button clearButton = new Button();
        FontAwesomeIconView clearIcon = new FontAwesomeIconView(FontAwesomeIcon.TIMES);
        clearIcon.setSize("12");
        clearButton.setGraphic(clearIcon);
        clearButton.getStyleClass().addAll("button", "clear-search-button");
        clearButton.setOnAction(e -> clearSearch());
        
        // Container do campo de pesquisa
        HBox searchContainer = new HBox(8);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.getChildren().addAll(searchIcon, searchField, clearButton);
        searchContainer.getStyleClass().add("search-container");
        
        // Histórico de pesquisas
        searchHistory.setPromptText("Pesquisas recentes");
        searchHistory.setPrefWidth(200);
        searchHistory.getStyleClass().add("search-history");
        
        // Container de filtros
        filtersContainer.setAlignment(Pos.CENTER_LEFT);
        filtersContainer.getStyleClass().add("filters-container");
        
        // Label de resultados
        resultsLabel.getStyleClass().add("results-label");
        updateResultsLabel();
        
        // Montagem do componente
        VBox searchSection = new VBox(8);
        searchSection.getChildren().addAll(searchContainer);
        
        if (showResultsCount) {
            HBox statusBar = new HBox();
            statusBar.setAlignment(Pos.CENTER_LEFT);
            statusBar.getChildren().addAll(resultsLabel);
            
            HBox historyContainer = new HBox(8);
            historyContainer.setAlignment(Pos.CENTER_RIGHT);
            
            Label historyLabel = new Label("Recentes:");
            historyLabel.getStyleClass().add("history-label");
            historyContainer.getChildren().addAll(historyLabel, searchHistory);
            
            HBox.setHgrow(statusBar, Priority.ALWAYS);
            
            HBox bottomBar = new HBox();
            bottomBar.getChildren().addAll(statusBar, historyContainer);
            searchSection.getChildren().add(bottomBar);
        }
        
        getChildren().addAll(searchSection, filtersContainer);
        setSpacing(12);
    }
    
    private void setupListeners() {
        // Listener do campo de pesquisa com debounce
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                performSearch(newVal);
                if (newVal != null && !newVal.trim().isEmpty()) {
                    addToHistory(newVal.trim());
                }
            });
        });
        
        // Listener do histórico
        searchHistory.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(searchField.getText())) {
                searchField.setText(newVal);
            }
        });
        
        // Listener da lista filtrada para atualizar contador
        filteredList.addListener((javafx.collections.ListChangeListener<T>) c -> {
            updateResultsLabel();
        });
    }
    
    private void setupStyles() {
        getStyleClass().add("advanced-search-component");
    }
    
    /**
     * Define o provedor de pesquisa personalizado
     */
    public void setSearchProvider(SearchProvider<T> provider) {
        this.searchProvider = provider;
    }
    
    /**
     * Adiciona um filtro disponível
     */
    public void addFilter(String id, String label, Predicate<T> filter) {
        FilterDefinition<T> filterDef = new FilterDefinition<>(id, label, filter);
        availableFilters.add(filterDef);
        
        ToggleButton filterButton = new ToggleButton(label);
        filterButton.getStyleClass().add("filter-chip");
        
        filterButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                activeFilters.put(id, filter);
                filterButton.getStyleClass().add("active");
            } else {
                activeFilters.remove(id);
                filterButton.getStyleClass().remove("active");
            }
            performSearch(searchField.getText());
        });
        
        filtersContainer.getChildren().add(filterButton);
    }
    
    /**
     * Executa a pesquisa com o texto fornecido
     */
    private void performSearch(String searchText) {
        filteredList.setPredicate(item -> {
            // Aplicar filtro de texto
            boolean matchesText = searchText == null || searchText.trim().isEmpty();
            
            if (!matchesText && searchProvider != null) {
                matchesText = searchProvider.matches(item, searchText);
            }
            
            // Aplicar filtros ativos
            boolean matchesFilters = activeFilters.values().stream()
                .allMatch(filter -> filter.test(item));
            
            return matchesText && matchesFilters;
        });
    }
    
    /**
     * Adiciona um termo ao histórico de pesquisas
     */
    private void addToHistory(String searchTerm) {
        if (searchHistoryList.contains(searchTerm)) {
            searchHistoryList.remove(searchTerm);
        }
        
        searchHistoryList.add(0, searchTerm);
        
        if (searchHistoryList.size() > maxHistoryItems) {
            searchHistoryList.remove(searchHistoryList.size() - 1);
        }
    }
    
    /**
     * Limpa a pesquisa atual
     */
    private void clearSearch() {
        searchField.clear();
        
        // Desativar filtros
        filtersContainer.getChildren().stream()
            .filter(node -> node instanceof ToggleButton)
            .map(node -> (ToggleButton) node)
            .forEach(button -> button.setSelected(false));
        
        activeFilters.clear();
    }
    
    /**
     * Atualiza o label de resultados
     */
    private void updateResultsLabel() {
        if (!showResultsCount) return;
        
        int total = sourceList.size();
        int filtered = filteredList.size();
        
        if (total == filtered) {
            resultsLabel.setText(String.format("Mostrando todos os %d itens", total));
        } else {
            resultsLabel.setText(String.format("Mostrando %d de %d itens", filtered, total));
        }
    }
    
    /**
     * Retorna a lista filtrada
     */
    public FilteredList<T> getFilteredList() {
        return filteredList;
    }
    
    /**
     * Obtém o campo de pesquisa para configurações adicionais
     */
    public TextField getSearchField() {
        return searchField;
    }
    
    /**
     * Define o prompt do campo de pesquisa
     */
    public void setSearchPrompt(String prompt) {
        this.searchPrompt = prompt;
        searchField.setPromptText(prompt);
    }
    
    /**
     * Define se deve mostrar o contador de resultados
     */
    public void setShowResultsCount(boolean show) {
        this.showResultsCount = show;
    }
    
    /**
     * Define o número máximo de itens no histórico
     */
    public void setMaxHistoryItems(int max) {
        this.maxHistoryItems = max;
    }
    
    /**
     * Interface para provedores de pesquisa customizados
     */
    @FunctionalInterface
    public interface SearchProvider<T> {
        boolean matches(T item, String searchText);
    }
    
    /**
     * Classe para definição de filtros
     */
    private static class FilterDefinition<T> {
        private final String id;
        private final String label;
        private final Predicate<T> predicate;
        
        public FilterDefinition(String id, String label, Predicate<T> predicate) {
            this.id = id;
            this.label = label;
            this.predicate = predicate;
        }
        
        public String getId() { return id; }
        public String getLabel() { return label; }
        public Predicate<T> getPredicate() { return predicate; }
    }
}
