package com.diagonal.cordeis.view;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.Predicate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Utilitários para melhorar a experiência do usuário (UX)
 * com animações, validações em tempo real e feedback visual.
 */
public class UXUtils {

    // ==========================================================
    // SISTEMA DE CORES CONSISTENTE
    // ==========================================================
    
    public static class Colors {
        public static final String PRIMARY = "#2196F3";
        public static final String PRIMARY_DARK = "#1976D2";
        public static final String PRIMARY_LIGHT = "#E3F2FD";
        public static final String ACCENT = "#FF5722";
        public static final String TEXT_PRIMARY = "#212121";
        public static final String TEXT_SECONDARY = "#757575";
        public static final String DIVIDER = "#BDBDBD";
        public static final String BACKGROUND_LIGHT = "#FAFAFA";
        public static final String CARD_BACKGROUND = "#FFFFFF";
        public static final String SUCCESS = "#4CAF50";
        public static final String WARNING = "#FFC107";
        public static final String ERROR = "#F44336";
        public static final String INFO = "#2196F3";
    }

    // ==========================================================
    // ANIMAÇÕES E TRANSIÇÕES
    // ==========================================================

    /**
     * Aplica uma animação de fade-in suave a um nó
     */
    public static void fadeIn(Node node, Duration duration) {
        if (node == null) return;
        
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Aplica uma animação de fade-in com duração padrão
     */
    public static void fadeIn(Node node) {
        fadeIn(node, Duration.millis(300));
    }

    /**
     * Aplica uma animação de slide-up suave
     */
    public static void slideUp(Node node, Duration duration) {
        if (node == null) return;
        
        node.setTranslateY(20);
        node.setOpacity(0);
        
        ParallelTransition parallel = new ParallelTransition();
        
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromY(20);
        slide.setToY(0);
        
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        parallel.getChildren().addAll(slide, fade);
        parallel.play();
    }

    /**
     * Aplica uma animação de slide-up com duração padrão
     */
    public static void slideUp(Node node) {
        slideUp(node, Duration.millis(400));
    }

    /**
     * Transição suave entre tabs/painéis
     */
    public static void transitionToPanel(Node fromPanel, Node toPanel, Duration duration) {
        if (fromPanel == null || toPanel == null) return;
        
        // Fade out do painel atual
        FadeTransition fadeOut = new FadeTransition(duration.divide(2), fromPanel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            fromPanel.setVisible(false);
            toPanel.setVisible(true);
            
            // Fade in do novo painel
            FadeTransition fadeIn = new FadeTransition(duration.divide(2), toPanel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
    }

    /**
     * Aplica efeito de hover em botões
     */
    public static void applyHoverEffect(Button button) {
        if (button == null) return;
        
        button.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
            
            // Adiciona sombra
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(0, 0, 0, 0.3));
            shadow.setRadius(8);
            shadow.setOffsetY(3);
            button.setEffect(shadow);
        });
        
        button.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
            
            // Remove sombra
            button.setEffect(null);
        });
    }

    /**
     * Aplica efeitos de microinterações em nós
     */
    public static void applyMicroInteractions(Node node) {
        if (node == null) return;
        
        node.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), node);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });
        
        node.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), node);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    // ==========================================================
    // VALIDAÇÃO EM TEMPO REAL AVANÇADA
    // ==========================================================

    /**
     * Aplica validação em tempo real a um TextField com debounce
     */
    public static void applyRealTimeValidation(TextField field, Validator validator, Duration debounceDelay) {
        if (field == null || validator == null) return;
        
        Timeline debounceTimer = new Timeline();
        
        field.textProperty().addListener((obs, oldText, newText) -> {
            debounceTimer.stop();
            debounceTimer.getKeyFrames().clear();
            
            KeyFrame keyFrame = new KeyFrame(debounceDelay, e -> {
                Platform.runLater(() -> {
                    ValidationResult result = validator.validate(newText);
                    applyValidationStyling(field, result);
                });
            });
            
            debounceTimer.getKeyFrames().add(keyFrame);
            debounceTimer.play();
        });
    }

    /**
     * Aplica validação em tempo real a um TextField
     */
    public static void applyRealTimeValidation(TextField field, Validator validator) {
        applyRealTimeValidation(field, validator, Duration.millis(300));
    }

    /**
     * Aplica estilos de validação a um campo de texto
     */
    public static void applyValidationStyling(TextField field, ValidationResult result) {
        if (field == null || result == null) return;
        
        // Remove estilos anteriores
        field.getStyleClass().removeAll("error", "success", "warning");
        
        switch (result.getType()) {
            case ERROR:
                field.getStyleClass().add("error");
                field.setTooltip(new Tooltip(result.getMessage()));
                break;
            case SUCCESS:
                field.getStyleClass().add("success");
                field.setTooltip(null);
                break;
            case WARNING:
                field.getStyleClass().add("warning");
                field.setTooltip(new Tooltip(result.getMessage()));
                break;
            default:
                field.setTooltip(null);
        }
    }

    /**
     * Cria um indicador de força para campos importantes
     */
    public static ProgressBar createStrengthIndicator(TextField field, StrengthValidator validator) {
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(200);
        strengthBar.setPrefHeight(8);
        
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            double strength = validator.calculateStrength(newVal);
            strengthBar.setProgress(strength);
            
            // Atualizar cor baseada na força
            strengthBar.getStyleClass().removeAll("weak", "medium", "strong");
            if (strength < 0.33) {
                strengthBar.getStyleClass().add("weak");
            } else if (strength < 0.67) {
                strengthBar.getStyleClass().add("medium");
            } else {
                strengthBar.getStyleClass().add("strong");
            }
        });
        
        return strengthBar;
    }

    // ==========================================================
    // LOADING STATES ELEGANTES
    // ==========================================================

    /**
     * Executa uma tarefa em background com indicador de carregamento
     */
    public static <T> CompletableFuture<T> executeWithLoading(
            Node parentNode, 
            String loadingMessage, 
            Supplier<T> task,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {
        
        if (parentNode == null || task == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        // Cria overlay de loading
        LoadingOverlay overlay = new LoadingOverlay(loadingMessage);
        overlay.show(parentNode);
        
        return CompletableFuture.supplyAsync(task)
            .whenComplete((result, throwable) -> {
                Platform.runLater(() -> {
                    overlay.hide();
                    
                    if (throwable != null) {
                        if (onError != null) {
                            onError.accept(throwable);
                        }
                    } else {
                        if (onSuccess != null) {
                            onSuccess.accept(result);
                        }
                    }
                });
            });
    }

    /**
     * Aplica loading state a um botão
     */
    public static void setButtonLoading(Button button, boolean loading) {
        if (button == null) return;
        
        if (loading) {
            button.setDisable(true);
            button.setText("Carregando...");
            
            // Adiciona um spinner
            ProgressIndicator spinner = new ProgressIndicator();
            spinner.setPrefSize(16, 16);
            button.setGraphic(spinner);
        } else {
            button.setDisable(false);
            button.setGraphic(null);
        }
    }

    /**
     * Cria um skeleton loader para listas
     */
    public static VBox createSkeletonLoader(int itemCount) {
        VBox skeleton = new VBox(8);
        skeleton.getStyleClass().add("skeleton-container");
        
        for (int i = 0; i < itemCount; i++) {
            HBox skeletonItem = new HBox(10);
            skeletonItem.getStyleClass().add("skeleton-item");
            skeletonItem.setPrefHeight(40);
            
            // Simula avatar/ícone
            Region avatar = new Region();
            avatar.setPrefSize(32, 32);
            avatar.getStyleClass().add("skeleton-avatar");
            
            // Simula texto
            VBox textContainer = new VBox(4);
            HBox.setHgrow(textContainer, Priority.ALWAYS);
            
            Region titleLine = new Region();
            titleLine.setPrefHeight(12);
            titleLine.getStyleClass().add("skeleton-text");
            
            Region subtitleLine = new Region();
            subtitleLine.setPrefHeight(10);
            subtitleLine.setPrefWidth(150);
            subtitleLine.getStyleClass().add("skeleton-text");
            
            textContainer.getChildren().addAll(titleLine, subtitleLine);
            skeletonItem.getChildren().addAll(avatar, textContainer);
            skeleton.getChildren().add(skeletonItem);
        }
        
        return skeleton;
    }

    // ==========================================================
    // FEEDBACK VISUAL E MICROINTERAÇÕES
    // ==========================================================

    /**
     * Aplica uma animação de "pulse" para chamar atenção
     */
    public static void pulseAttention(Node node) {
        if (node == null) return;
        
        ScaleTransition pulse1 = new ScaleTransition(Duration.millis(200), node);
        pulse1.setToX(1.1);
        pulse1.setToY(1.1);
        
        ScaleTransition pulse2 = new ScaleTransition(Duration.millis(200), node);
        pulse2.setToX(1.0);
        pulse2.setToY(1.0);
        
        SequentialTransition sequence = new SequentialTransition(pulse1, pulse2);
        sequence.setCycleCount(2);
        sequence.play();
    }

    /**
     * Anima a mudança de cor de fundo
     */
    public static void animateBackgroundColor(Node node, String fromColor, String toColor, Duration duration) {
        if (node == null) return;
        
        Timeline timeline = new Timeline();
        
        KeyValue keyValue = new KeyValue(node.styleProperty(), 
            "-fx-background-color: " + toColor + ";");
        KeyFrame keyFrame = new KeyFrame(duration, keyValue);
        
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    /**
     * Cria um indicador de toast/notificação temporária
     */
    public static void showToast(Node parentNode, String message, ToastType type, Duration duration) {
        if (parentNode == null || message == null) return;
        
        Label toastLabel = new Label(message);
        toastLabel.getStyleClass().addAll("toast", type.getStyleClass());
        toastLabel.setPrefHeight(40);
        toastLabel.setMaxWidth(300);
        toastLabel.setWrapText(true);
        
        // Adicionar ícone baseado no tipo
        FontAwesomeIconView icon = new FontAwesomeIconView(type.getIcon());
        icon.setSize("14");
        icon.setFill(Color.WHITE);
        toastLabel.setGraphic(icon);
        
        // Posicionar no topo direito
        if (parentNode instanceof StackPane) {
            StackPane parent = (StackPane) parentNode;
            parent.getChildren().add(toastLabel);
            StackPane.setAlignment(toastLabel, Pos.TOP_RIGHT);
            StackPane.setMargin(toastLabel, new javafx.geometry.Insets(20));
        }
        
        // Animação de entrada
        slideUp(toastLabel, Duration.millis(300));
        
        // Remover após o tempo especificado
        Timeline removeTimer = new Timeline(new KeyFrame(duration, e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> {
                if (parentNode instanceof StackPane) {
                    ((StackPane) parentNode).getChildren().remove(toastLabel);
                }
            });
            fadeOut.play();
        }));
        removeTimer.play();
    }

    // ==========================================================
    // AUTOCOMPLETE E SUGESTÕES INTELIGENTES
    // ==========================================================

    /**
     * Adiciona funcionalidade de autocomplete a um ComboBox
     */
    public static void makeComboBoxSearchable(ComboBox<String> comboBox) {
        if (comboBox == null) return;
        
        comboBox.setEditable(true);
        
        comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                comboBox.hide();
                return;
            }
            
            Platform.runLater(() -> {
                comboBox.getItems().filtered(item -> 
                    item.toLowerCase().contains(newText.toLowerCase())
                );
                
                if (!comboBox.getItems().isEmpty()) {
                    comboBox.show();
                }
            });
        });
    }

    /**
     * Cria um campo de pesquisa avançado com filtros
     */
    public static <T> AdvancedSearchField<T> createAdvancedSearchField(
            ObservableList<T> sourceList,
            SearchConfiguration<T> config) {
        return new AdvancedSearchField<>(sourceList, config);
    }

    // ==========================================================
    // NAVEGAÇÃO E BREADCRUMB
    // ==========================================================

    /**
     * Cria um componente de breadcrumb interativo
     */
    public static HBox createBreadcrumb(String... items) {
        return createBreadcrumb(null, items);
    }

    /**
     * Cria um componente de breadcrumb com callback de navegação
     */
    public static HBox createBreadcrumb(Consumer<String> onNavigate, String... items) {
        HBox breadcrumb = new HBox(5);
        breadcrumb.getStyleClass().add("breadcrumb-container");
        breadcrumb.setAlignment(Pos.CENTER_LEFT);
        
        for (int i = 0; i < items.length; i++) {
            Label item = new Label(items[i]);
            item.getStyleClass().add("breadcrumb-item");
            
            if (i == items.length - 1) {
                item.getStyleClass().add("current");
            } else {
                // Adiciona clique para navegação
                item.setOnMouseClicked(e -> {
                    if (onNavigate != null) {
                        onNavigate.accept(item.getText());
                    }
                });
                
                // Adiciona hover effect
                item.setOnMouseEntered(e -> item.getStyleClass().add("hover"));
                item.setOnMouseExited(e -> item.getStyleClass().remove("hover"));
            }
            
            breadcrumb.getChildren().add(item);
            
            // Adiciona separador (exceto no último item)
            if (i < items.length - 1) {
                FontAwesomeIconView separator = new FontAwesomeIconView(FontAwesomeIcon.CHEVRON_RIGHT);
                separator.setSize("10");
                separator.setFill(Color.valueOf("#BDBDBD"));
                separator.getStyleClass().add("breadcrumb-separator");
                breadcrumb.getChildren().add(separator);
            }
        }
        
        return breadcrumb;
    }

    // ==========================================================
    // ACESSIBILIDADE E RESPONSIVIDADE
    // ==========================================================

    /**
     * Aplica suporte a atalhos de teclado
     */
    public static void addKeyboardShortcut(Node node, KeyboardShortcut shortcut, Runnable action) {
        if (node == null || shortcut == null || action == null) return;
        
        node.setOnKeyPressed(event -> {
            if (shortcut.matches(event)) {
                action.run();
                event.consume();
            }
        });
    }

    /**
     * Aplica indicador de foco para acessibilidade
     */
    public static void applyAccessibilityFocus(Node node) {
        if (node == null) return;
        
        node.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                node.getStyleClass().add("focus-indicator");
            } else {
                node.getStyleClass().remove("focus-indicator");
            }
        });
    }

    /**
     * Ajusta o tamanho da fonte para acessibilidade
     */
    public static void adjustFontSize(Node node, FontSizeMode mode) {
        if (node == null) return;
        
        switch (mode) {
            case LARGE:
                node.getStyleClass().add("large-text");
                break;
            case EXTRA_LARGE:
                node.getStyleClass().add("extra-large-text");
                break;
            case NORMAL:
            default:
                node.getStyleClass().removeAll("large-text", "extra-large-text");
                break;
        }
    }

    // ==========================================================
    // PERFORMANCE E OTIMIZAÇÃO
    // ==========================================================

    /**
     * Aplica carregamento progressivo a uma lista
     */
    public static <T> void applyProgressiveLoading(ListView<T> listView, 
            Supplier<List<T>> dataLoader, int batchSize) {
        if (listView == null || dataLoader == null) return;
        
        ObservableList<T> items = FXCollections.observableArrayList();
        listView.setItems(items);
        
        // Carrega primeiro lote
        CompletableFuture.supplyAsync(dataLoader)
            .thenAccept(data -> {
                Platform.runLater(() -> {
                    items.addAll(data.subList(0, Math.min(batchSize, data.size())));
                });
            });
        
        // Adiciona listener para carregar mais itens quando necessário
        listView.scrollTo(0);
        // Implementar lógica de scroll infinito aqui se necessário
    }

    // ==========================================================
    // CLASSES AUXILIARES E ENUMS
    // ==========================================================

    public enum ValidationType {
        ERROR, WARNING, SUCCESS, NONE
    }

    public enum ToastType {
        SUCCESS("toast-success", FontAwesomeIcon.CHECK_CIRCLE),
        ERROR("toast-error", FontAwesomeIcon.TIMES_CIRCLE),
        WARNING("toast-warning", FontAwesomeIcon.EXCLAMATION_TRIANGLE),
        INFO("toast-info", FontAwesomeIcon.INFO_CIRCLE);
        
        private final String styleClass;
        private final FontAwesomeIcon icon;
        
        ToastType(String styleClass, FontAwesomeIcon icon) {
            this.styleClass = styleClass;
            this.icon = icon;
        }
        
        public String getStyleClass() { return styleClass; }
        public FontAwesomeIcon getIcon() { return icon; }
    }

    public enum FontSizeMode {
        NORMAL, LARGE, EXTRA_LARGE
    }

    public static class ValidationResult {
        private final ValidationType type;
        private final String message;

        public ValidationResult(ValidationType type, String message) {
            this.type = type;
            this.message = message;
        }

        public ValidationType getType() { return type; }
        public String getMessage() { return message; }

        public static ValidationResult success() {
            return new ValidationResult(ValidationType.SUCCESS, "");
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(ValidationType.ERROR, message);
        }

        public static ValidationResult warning(String message) {
            return new ValidationResult(ValidationType.WARNING, message);
        }
    }

    @FunctionalInterface
    public interface Validator {
        ValidationResult validate(String value);
    }

    @FunctionalInterface
    public interface StrengthValidator {
        double calculateStrength(String value);
    }

    public static class KeyboardShortcut {
        private final javafx.scene.input.KeyCode keyCode;
        private final boolean ctrl;
        private final boolean shift;
        private final boolean alt;
        
        public KeyboardShortcut(javafx.scene.input.KeyCode keyCode, boolean ctrl, boolean shift, boolean alt) {
            this.keyCode = keyCode;
            this.ctrl = ctrl;
            this.shift = shift;
            this.alt = alt;
        }
        
        public static KeyboardShortcut ctrl(javafx.scene.input.KeyCode keyCode) {
            return new KeyboardShortcut(keyCode, true, false, false);
        }
        
        public boolean matches(javafx.scene.input.KeyEvent event) {
            return event.getCode() == keyCode &&
                   event.isControlDown() == ctrl &&
                   event.isShiftDown() == shift &&
                   event.isAltDown() == alt;
        }
    }

    // ==========================================================
    // CLASSES AUXILIARES COMPLEXAS
    // ==========================================================

    public static class AdvancedSearchField<T> extends VBox {
        private final TextField searchField;
        private final HBox filtersContainer;
        private final FilteredList<T> filteredList;
        private final SearchConfiguration<T> config;
        private final Map<String, Predicate<T>> activeFilters = new HashMap<>();
        
        public AdvancedSearchField(ObservableList<T> sourceList, SearchConfiguration<T> config) {
            this.config = config;
            this.filteredList = new FilteredList<>(sourceList);
            
            this.searchField = new TextField();
            this.filtersContainer = new HBox(8);
            
            setupComponents();
            setupListeners();
        }
        
        private void setupComponents() {
            searchField.setPromptText(config.getSearchPrompt());
            searchField.getStyleClass().add("advanced-search-field");
            
            filtersContainer.setAlignment(Pos.CENTER_LEFT);
            filtersContainer.getStyleClass().add("filters-container");
            
            getChildren().addAll(searchField, filtersContainer);
            setSpacing(8);
        }
        
        private void setupListeners() {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                updateFilter();
            });
        }
        
        private void updateFilter() {
            filteredList.setPredicate(item -> {
                // Aplicar filtro de texto
                String searchText = searchField.getText();
                boolean matchesText = searchText == null || searchText.isEmpty() ||
                    config.getTextMatcher().test(item, searchText);
                
                // Aplicar filtros ativos
                boolean matchesFilters = activeFilters.values().stream()
                    .allMatch(filter -> filter.test(item));
                
                return matchesText && matchesFilters;
            });
        }
        
        public void addFilter(String name, String label, Predicate<T> filter) {
            ToggleButton filterButton = new ToggleButton(label);
            filterButton.getStyleClass().add("filter-chip");
            
            filterButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    activeFilters.put(name, filter);
                    filterButton.getStyleClass().add("active");
                } else {
                    activeFilters.remove(name);
                    filterButton.getStyleClass().remove("active");
                }
                updateFilter();
            });
            
            filtersContainer.getChildren().add(filterButton);
        }
        
        public FilteredList<T> getFilteredList() {
            return filteredList;
        }
        
        public TextField getSearchField() {
            return searchField;
        }
    }

    public static class SearchConfiguration<T> {
        private String searchPrompt = "Pesquisar...";
        private TextMatcher<T> textMatcher;
        
        public SearchConfiguration(TextMatcher<T> textMatcher) {
            this.textMatcher = textMatcher;
        }
        
        public String getSearchPrompt() { return searchPrompt; }
        public void setSearchPrompt(String searchPrompt) { this.searchPrompt = searchPrompt; }
        public TextMatcher<T> getTextMatcher() { return textMatcher; }
        
        @FunctionalInterface
        public interface TextMatcher<T> {
            boolean test(T item, String searchText);
        }
    }

    public static class LoadingOverlay {
        private final StackPane overlay;
        private final ProgressIndicator spinner;
        private final Label messageLabel;
        
        public LoadingOverlay(String message) {
            overlay = new StackPane();
            overlay.getStyleClass().add("loading-overlay");
            
            VBox content = new VBox(10);
            content.setAlignment(Pos.CENTER);
            
            spinner = new ProgressIndicator();
            spinner.getStyleClass().add("loading-spinner");
            
            messageLabel = new Label(message);
            messageLabel.getStyleClass().add("loading-text");
            
            content.getChildren().addAll(spinner, messageLabel);
            overlay.getChildren().add(content);
        }
        
        public void show(Node parent) {
            if (parent instanceof StackPane) {
                StackPane parentPane = (StackPane) parent;
                parentPane.getChildren().add(overlay);
                fadeIn(overlay, Duration.millis(200));
            }
        }
        
        public void hide() {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                if (overlay.getParent() instanceof StackPane) {
                    ((StackPane) overlay.getParent()).getChildren().remove(overlay);
                }
            });
            fadeOut.play();
        }
        
        public void updateMessage(String message) {
            messageLabel.setText(message);
        }
    }
    
    /**
     * Aplica uma animação de fade-out a um nó
     */
    public static void fadeOut(Node node, Duration duration, Runnable onFinished) {
        if (node == null) return;
        
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        if (onFinished != null) {
            fade.setOnFinished(e -> onFinished.run());
        }
        fade.play();
    }

    /**
     * Aplica uma animação de fade-out com duração padrão
     */
    public static void fadeOut(Node node) {
        fadeOut(node, Duration.millis(300), null);
    }
}
