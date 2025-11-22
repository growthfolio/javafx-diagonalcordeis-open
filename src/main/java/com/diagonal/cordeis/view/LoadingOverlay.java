package com.diagonal.cordeis.view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Componente de overlay para indicar carregamento
 */
public class LoadingOverlay {
    
    private final StackPane overlay;
    private final String message;
    
    public LoadingOverlay(String message) {
        this.message = message;
        this.overlay = createOverlay();
    }
    
    private StackPane createOverlay() {
        StackPane pane = new StackPane();
        pane.getStyleClass().add("loading-overlay");
        pane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9);");
        
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.getStyleClass().add("loading-spinner");
        spinner.setPrefSize(40, 40);
        
        Label loadingText = new Label(message != null ? message : "Carregando...");
        loadingText.getStyleClass().add("loading-text");
        loadingText.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-font-weight: bold;");
        
        content.getChildren().addAll(spinner, loadingText);
        pane.getChildren().add(content);
        
        return pane;
    }
    
    public void show(Node parent) {
        if (parent instanceof StackPane) {
            StackPane stackPane = (StackPane) parent;
            stackPane.getChildren().add(overlay);
        }
    }
    
    public void hide() {
        if (overlay.getParent() instanceof StackPane) {
            StackPane parent = (StackPane) overlay.getParent();
            parent.getChildren().remove(overlay);
        }
    }
}
