package com.diagonal.cordeis.view;

import com.diagonal.cordeis.dto.LivroDTO;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.util.List;
import java.util.function.Consumer;

/**
 * Componente de visualização em galeria para foco nas capas dos livros
 * Implementa modo galeria como alternativa à visualização em tabela
 */
public class GalleryViewComponent extends ScrollPane {
    
    private final TilePane tilePane;
    private final List<LivroDTO> livros;
    private Consumer<LivroDTO> onLivroSelected;
    private Consumer<LivroDTO> onLivroDoubleClicked;
    private LivroDTO selectedLivro;
    
    // Configurações da galeria
    private double cardWidth = 180;
    private double cardHeight = 240;
    private double spacing = 15;
    
    public GalleryViewComponent(List<LivroDTO> livros) {
        this.livros = livros;
        this.tilePane = new TilePane();
        
        setupComponent();
        populateGallery();
    }
    
    private void setupComponent() {
        // Configurar ScrollPane
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        getStyleClass().add("gallery-scroll-pane");
        
        // Configurar TilePane
        tilePane.setPrefColumns(4); // Calculado dinamicamente
        tilePane.setHgap(spacing);
        tilePane.setVgap(spacing);
        tilePane.setPadding(new Insets(20));
        tilePane.setAlignment(Pos.TOP_CENTER);
        
        setContent(tilePane);
        
        // Listener para redimensionamento dinâmico
        widthProperty().addListener((obs, oldWidth, newWidth) -> {
            updateColumnsCount(newWidth.doubleValue());
        });
    }
    
    private void updateColumnsCount(double width) {
        int columns = Math.max(1, (int) ((width - 40) / (cardWidth + spacing)));
        tilePane.setPrefColumns(columns);
    }
    
    private void populateGallery() {
        Platform.runLater(() -> {
            tilePane.getChildren().clear();
            
            for (LivroDTO livro : livros) {
                VBox card = createBookCard(livro);
                tilePane.getChildren().add(card);
                
                // Aplicar animação de entrada
                UXUtils.fadeIn(card, Duration.millis(300));
            }
        });
    }
    
    private VBox createBookCard(LivroDTO livro) {
        VBox card = new VBox(8);
        card.setPrefWidth(cardWidth);
        card.setPrefHeight(cardHeight);
        card.getStyleClass().add("gallery-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(10));
        
        // Container para a imagem/ícone do livro
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefWidth(cardWidth - 20);
        imageContainer.setPrefHeight(160);
        imageContainer.getStyleClass().add("book-image-container");
        
        // Por enquanto, usar ícone genérico (pode ser melhorado para mostrar preview do PDF)
        FontAwesomeIconView bookIcon = new FontAwesomeIconView(FontAwesomeIcon.BOOK);
        bookIcon.setSize("48");
        bookIcon.setFill(javafx.scene.paint.Color.valueOf(UXUtils.Colors.PRIMARY));
        imageContainer.getChildren().add(bookIcon);
        
        // Título do livro
        Label titleLabel = new Label(livro.getTitulo());
        titleLabel.getStyleClass().add("gallery-book-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(cardWidth - 20);
        titleLabel.setAlignment(Pos.CENTER);
        
        // SKU do livro
        Label skuLabel = new Label("SKU: " + livro.getSku());
        skuLabel.getStyleClass().add("gallery-book-sku");
        
        // Tipo e páginas
        HBox infoBox = new HBox(8);
        infoBox.setAlignment(Pos.CENTER);
        
        Label typeLabel = new Label(livro.getTipo().toString());
        typeLabel.getStyleClass().add("gallery-book-type");
        
        Label pagesLabel = new Label(livro.getPaginas() + " pág.");
        pagesLabel.getStyleClass().add("gallery-book-pages");
        
        infoBox.getChildren().addAll(typeLabel, new Label("•"), pagesLabel);
        
        card.getChildren().addAll(imageContainer, titleLabel, skuLabel, infoBox);
        
        // Configurar eventos
        setupCardEvents(card, livro);
        
        return card;
    }
    
    private void setupCardEvents(VBox card, LivroDTO livro) {
        // Hover effects
        card.setOnMouseEntered(e -> {
            card.getStyleClass().add("gallery-card-hover");
            UXUtils.applyMicroInteractions(card);
        });
        
        card.setOnMouseExited(e -> {
            card.getStyleClass().remove("gallery-card-hover");
        });
        
        // Seleção
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                selectCard(card, livro);
                if (onLivroSelected != null) {
                    onLivroSelected.accept(livro);
                }
            } else if (e.getClickCount() == 2) {
                if (onLivroDoubleClicked != null) {
                    onLivroDoubleClicked.accept(livro);
                }
            }
        });
    }
    
    private void selectCard(VBox card, LivroDTO livro) {
        // Remover seleção anterior
        tilePane.getChildren().forEach(node -> {
            node.getStyleClass().remove("gallery-card-selected");
        });
        
        // Aplicar seleção ao card atual
        card.getStyleClass().add("gallery-card-selected");
        selectedLivro = livro;
        
        // Animação de seleção
        UXUtils.pulseAttention(card);
    }
    
    /**
     * Atualiza a galeria com uma nova lista de livros
     */
    public void updateBooks(List<LivroDTO> newLivros) {
        livros.clear();
        livros.addAll(newLivros);
        populateGallery();
    }
    
    /**
     * Filtrar livros na galeria
     */
    public void filterBooks(String searchText) {
        List<LivroDTO> filteredBooks = livros.stream()
            .filter(livro -> 
                livro.getTitulo().toLowerCase().contains(searchText.toLowerCase()) ||
                livro.getSku().toLowerCase().contains(searchText.toLowerCase())
            )
            .toList();
        
        Platform.runLater(() -> {
            tilePane.getChildren().clear();
            for (LivroDTO livro : filteredBooks) {
                VBox card = createBookCard(livro);
                tilePane.getChildren().add(card);
                UXUtils.slideUp(card, Duration.millis(200));
            }
        });
    }
    
    // Getters e Setters
    public void setOnLivroSelected(Consumer<LivroDTO> onLivroSelected) {
        this.onLivroSelected = onLivroSelected;
    }
    
    public void setOnLivroDoubleClicked(Consumer<LivroDTO> onLivroDoubleClicked) {
        this.onLivroDoubleClicked = onLivroDoubleClicked;
    }
    
    public LivroDTO getSelectedLivro() {
        return selectedLivro;
    }
    
    public void setCardSize(double width, double height) {
        this.cardWidth = width;
        this.cardHeight = height;
        populateGallery();
    }
    
    public void setSpacing(double spacing) {
        this.spacing = spacing;
        tilePane.setHgap(spacing);
        tilePane.setVgap(spacing);
    }
}
