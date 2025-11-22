package com.diagonal.cordeis.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Container especializado para conteúdo de modais,
 * garantindo espaçamento e dimensionamento apropriados.
 */
public class ModalContainer extends StackPane {
    
    private static final double DEFAULT_PADDING = 20;
    
    /**
     * Cria um novo container para modal com o nó fornecido
     */
    private ModalContainer(Node content) {
        // Configurar padding e espaçamento
        setPadding(new Insets(DEFAULT_PADDING));
        
        // Garantir que o conteúdo se expanda adequadamente
        if (content instanceof Region) {
            Region region = (Region) content;
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
        }
        
        getChildren().add(content);
    }
    
    /**
     * Envolve um nó em um container modal apropriado
     */
    public static ModalContainer wrap(Node content) {
        return new ModalContainer(content);
    }
}
