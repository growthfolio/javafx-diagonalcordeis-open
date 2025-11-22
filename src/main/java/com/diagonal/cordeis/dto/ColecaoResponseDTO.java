package com.diagonal.cordeis.dto;

import com.diagonal.cordeis.model.Colecao;
import javafx.beans.property.*;

import java.util.List;
import java.util.stream.Collectors;

public class ColecaoResponseDTO {
    
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty nome = new SimpleStringProperty();
    private final StringProperty sku = new SimpleStringProperty();
    private final IntegerProperty quantidadeLivros = new SimpleIntegerProperty();
    private final StringProperty nomesLivrosTexto = new SimpleStringProperty();

    public ColecaoResponseDTO() {}

    public ColecaoResponseDTO(Long id, String nome, String sku, List<String> nomesLivros) {
        this.id.set(id != null ? id : 0L);
        this.nome.set(nome);
        this.sku.set(sku);
        this.quantidadeLivros.set(nomesLivros != null ? nomesLivros.size() : 0);
        this.nomesLivrosTexto.set(nomesLivros != null ? String.join(", ", nomesLivros) : "");
    }

    // Properties para JavaFX
    public LongProperty idProperty() { return id; }
    public StringProperty nomeProperty() { return nome; }
    public StringProperty skuProperty() { return sku; }
    public IntegerProperty quantidadeLivrosProperty() { return quantidadeLivros; }
    public StringProperty nomesLivrosTextoProperty() { return nomesLivrosTexto; }

    // Getters e Setters
    public Long getId() { return id.get(); }
    public void setId(Long id) { this.id.set(id != null ? id : 0L); }

    public String getNome() { return nome.get(); }
    public void setNome(String nome) { this.nome.set(nome); }

    public String getSku() { return sku.get(); }
    public void setSku(String sku) { this.sku.set(sku); }

    public int getQuantidadeLivros() { return quantidadeLivros.get(); }
    public void setQuantidadeLivros(int quantidade) { this.quantidadeLivros.set(quantidade); }

    public String getNomesLivrosTexto() { return nomesLivrosTexto.get(); }
    public void setNomesLivrosTexto(String nomesTexto) { this.nomesLivrosTexto.set(nomesTexto); }

    // Métodos de conversão
    public static ColecaoResponseDTO fromModel(Colecao colecao) {
        List<String> nomesLivros = colecao.getLivros().stream()
                .map(livro -> livro.getTitulo())
                .collect(Collectors.toList());
        
        return new ColecaoResponseDTO(
            colecao.getId(),
            colecao.getNome(),
            colecao.getSku(),
            nomesLivros
        );
    }

    @Override
    public String toString() {
        return nome.get() + " (" + sku.get() + ")";
    }
}
