package com.diagonal.cordeis.dto;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * 
 * DTO para exibição de coleções na tabela de impressão com checkbox de seleção
 * 
 */
public class ColecaoImpressaoDTO {
    private final BooleanProperty selecionada = new SimpleBooleanProperty(false);
    private final ColecaoResponseDTO colecao;

    public ColecaoImpressaoDTO(ColecaoResponseDTO colecao) {
        this.colecao = colecao;
    }

    public ColecaoImpressaoDTO(ColecaoDTO dto) {
        this.colecao = new ColecaoResponseDTO(
            dto.getId(),
            dto.getNome(),
            dto.getSku(),
            dto.getLivros().stream()
                .map(livro -> livro.getTitulo())
                .toList()
        );
    }

    public BooleanProperty selecionadaProperty() {
        return selecionada;
    }

    public boolean isSelecionada() {
        return selecionada.get();
    }

    public void setSelecionada(boolean selecionada) {
        this.selecionada.set(selecionada);
    }

    public ColecaoResponseDTO getColecao() {
        return colecao;
    }

    // Delegates para propriedades da coleção
    public Long getId() {
        return colecao.getId();
    }

    public String getSku() {
        return colecao.getSku();
    }

    public String getNome() {
        return colecao.getNome();
    }

    public Integer getQuantidadeLivros() {
        return colecao.getQuantidadeLivros();
    }

    public String getNomesLivros() {
        return colecao.getNomesLivrosTexto();
    }

    @Override
    public String toString() {
        return colecao.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ColecaoImpressaoDTO that = (ColecaoImpressaoDTO) obj;
        return colecao.equals(that.colecao);
    }

    @Override
    public int hashCode() {
        return colecao.hashCode();
    }
}
