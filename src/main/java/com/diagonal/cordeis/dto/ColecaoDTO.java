package com.diagonal.cordeis.dto;

import com.diagonal.cordeis.model.Colecao;
import com.diagonal.cordeis.model.TipoLivro;
import javafx.beans.property.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class ColecaoDTO {
    
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty nome = new SimpleStringProperty();
    private final StringProperty sku = new SimpleStringProperty();
    private final IntegerProperty quantidadeLivros = new SimpleIntegerProperty();
    private final StringProperty nomesLivros = new SimpleStringProperty();
    private List<LivroDTO> livros = new ArrayList<>();

    // Propriedades JavaFX
    public LongProperty idProperty() { return id; }
    public StringProperty nomeProperty() { return nome; }
    public StringProperty skuProperty() { return sku; }
    public IntegerProperty quantidadeLivrosProperty() { return quantidadeLivros; }
    public StringProperty nomesLivrosProperty() { return nomesLivros; }

    // Getters e Setters
    public long getId() { return id.get(); }
    public void setId(long id) { this.id.set(id); }

    public String getNome() { return nome.get(); }
    public void setNome(String nome) { this.nome.set(nome); }

    public String getSku() { return sku.get(); }
    public void setSku(String sku) { this.sku.set(sku); }

    public int getQuantidadeLivros() { return quantidadeLivros.get(); }
    public void setQuantidadeLivros(int quantidadeLivros) { this.quantidadeLivros.set(quantidadeLivros); }

    public String getNomesLivros() { return nomesLivros.get(); }
    public void setNomesLivros(String nomesLivros) { this.nomesLivros.set(nomesLivros); }
    
    public List<LivroDTO> getLivros() { return livros; }
    public void setLivros(List<LivroDTO> livros) { this.livros = livros; }

    // Conversão: Entidade -> DTO
    public static ColecaoDTO fromModel(Colecao colecao) {
        ColecaoDTO dto = new ColecaoDTO();
        dto.setId(colecao.getId() != null ? colecao.getId() : 0L);
        dto.setNome(colecao.getNome());
        dto.setSku(colecao.getSku());
        dto.setQuantidadeLivros(colecao.getQuantidadeLivros());
        
        // Criar string com nomes dos livros
        List<String> nomes = colecao.getLivros().stream()
                .map(livro -> livro.getTitulo())
                .sorted()
                .toList();
        dto.setNomesLivros(String.join(", ", nomes));
        
        return dto;
    }

    @Override
    public String toString() {
        return nome.get() + " (" + sku.get() + ")";
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LivroDTO {
        private Long id;
        private String titulo;
        private String sku;
        private TipoLivro tipo;
    }
}
