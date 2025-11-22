package com.diagonal.cordeis.dto;

import com.diagonal.cordeis.model.Livro;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * DTO para exibição de livros na tabela de impressão com checkbox de seleção
 */
public class LivroImpressaoDTO {
    private final BooleanProperty selecionado = new SimpleBooleanProperty(false);
    private final LivroDTO livro;

    public LivroImpressaoDTO(LivroDTO livro) {
        this.livro = livro;
    }

    public BooleanProperty selecionadoProperty() {
        return selecionado;
    }

    public boolean isSelecionado() {
        return selecionado.get();
    }

    public void setSelecionado(boolean selecionado) {
        this.selecionado.set(selecionado);
    }

    public LivroDTO getLivro() {
        return livro;
    }

    // Delegates para propriedades do livro
    public Long getId() {
        return livro.getId();
    }

    public String getSku() {
        return livro.getSku();
    }

    public String getTitulo() {
        return livro.getTitulo();
    }

    public String getTipo() {
        return livro.getTipo() != null ? livro.getTipo().toString() : "";
    }

    public Integer getPaginas() {
        return livro.getPaginas();
    }

    public String getNomeArquivo() {
        return livro.getNomeArquivo();
    }

    public String getCaminhoPdf() {
        return livro.getCaminhoPdf();
    }

    public Livro toModel() {
        return livro.toModel();
    }

    @Override
    public String toString() {
        return livro.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LivroImpressaoDTO that = (LivroImpressaoDTO) obj;
        return livro.equals(that.livro);
    }

    @Override
    public int hashCode() {
        return livro.hashCode();
    }
}
