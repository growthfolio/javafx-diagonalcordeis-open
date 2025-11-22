package com.diagonal.cordeis.view;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilitário para gerenciar modais de forma consistente,
 * garantindo o dimensionamento automático e correto.
 */
@Slf4j
public class ModalUtils {
    
    /**
     * Valores padrão para dimensionamento de modais
     */
    private static final double MIN_WIDTH = 400;
    private static final double MIN_HEIGHT = 300;
    private static final double PADDING = 20;

    /**
     * Configura um Stage modal com dimensionamento automático
     */
    public static Stage createModalStage(String title, Node content, Window owner) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            modalStage.initOwner(owner);
        }
        modalStage.setTitle(title);

        // Criar a cena com o conteúdo
        Scene scene = new Scene(ModalContainer.wrap(content));
        modalStage.setScene(scene);

        // Configurar dimensionamento automático
        configureModalSizing(modalStage, content);

        return modalStage;
    }

    /**
     * Cria um modal de autenticação com dimensionamento automático otimizado
     */
    public static Stage createAuthModalStage(String title, Parent content, Window owner) {
        Stage modalStage = new Stage();
        modalStage.setTitle(title);
        modalStage.initOwner(owner);
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UNDECORATED);
        modalStage.setResizable(false);

        // Criar a cena sem definir tamanho
        Scene scene = new Scene(content);
        modalStage.setScene(scene);

        // Dimensionamento automático baseado no conteúdo
        modalStage.sizeToScene();
        
        // Configurar tamanhos mínimos apenas para autenticação
        modalStage.setMinWidth(350);
        modalStage.setMinHeight(400);

        return modalStage;
    }

    /**
     * Configura o dimensionamento automático para um modal
     */
    public static void configureModalSizing(Stage stage, Node content) {
        // Permitir redimensionamento
        stage.setResizable(true);

        // Configurar tamanhos mínimos
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        // Ajustar tamanho com base no conteúdo
        stage.sizeToScene();

        // Garantir que o modal não seja maior que 90% da tela
        stage.widthProperty().addListener((obs, old, newWidth) -> {
            Rectangle2D bounds = stage.getOwner() != null ? 
                new Rectangle2D(0, 0, stage.getOwner().getWidth(), stage.getOwner().getHeight()) : 
                Screen.getPrimary().getVisualBounds();
            if (newWidth.doubleValue() > bounds.getWidth() * 0.9) {
                stage.setWidth(bounds.getWidth() * 0.9);
            }
        });

        stage.heightProperty().addListener((obs, old, newHeight) -> {
            Rectangle2D bounds = stage.getOwner() != null ? 
                new Rectangle2D(0, 0, stage.getOwner().getWidth(), stage.getOwner().getHeight()) : 
                Screen.getPrimary().getVisualBounds();
            if (newHeight.doubleValue() > bounds.getHeight() * 0.9) {
                stage.setHeight(bounds.getHeight() * 0.9);
            }
        });

        // Centralizar na tela
        stage.centerOnScreen();
    }

    /**
     * Configura uma Dialog com dimensionamento automático
     */
    public static void configureDialogSizing(Dialog<?> dialog) {
        // Garantir que o diálogo seja redimensionável
        dialog.getDialogPane().setMinWidth(MIN_WIDTH);
        dialog.getDialogPane().setMinHeight(MIN_HEIGHT);
        
        // Configurar dimensionamento automático
        dialog.getDialogPane().expandedProperty().addListener((obs, old, newValue) -> {
            dialog.getDialogPane().requestLayout();
        });

        // Ajustar ao conteúdo
        Platform.runLater(() -> {
            dialog.getDialogPane().requestLayout();
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.sizeToScene();
            stage.centerOnScreen();
        });
    }
}
