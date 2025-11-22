package com.diagonal.cordeis.dto;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ArquivoImportadoDTO {

    private final StringProperty titulo = new SimpleStringProperty();
    private final StringProperty caminho = new SimpleStringProperty();
    private final StringProperty tipo = new SimpleStringProperty();
    private final IntegerProperty paginas = new SimpleIntegerProperty();

    public ArquivoImportadoDTO() {}

    public ArquivoImportadoDTO(String caminho, String titulo, String tipo, int paginas) {
        this.caminho.set(caminho);
        this.titulo.set(titulo);
        this.tipo.set(tipo);
        this.paginas.set(paginas);
    }

    // Título
    public String getTitulo() {
        return titulo.get();
    }

    public void setTitulo(String titulo) {
        this.titulo.set(titulo);
    }

    public StringProperty tituloProperty() {
        return titulo;
    }

    // Caminho
    public String getCaminho() {
        return caminho.get();
    }

    public void setCaminho(String caminho) {
        this.caminho.set(caminho);
    }

    public StringProperty caminhoProperty() {
        return caminho;
    }

    // Tipo
    public String getTipo() {
        return tipo.get();
    }

    public void setTipo(String tipo) {
        this.tipo.set(tipo);
    }

    public StringProperty tipoProperty() {
        return tipo;
    }

    // Páginas
    public int getPaginas() {
        return paginas.get();
    }

    public void setPaginas(int paginas) {
        this.paginas.set(paginas);
    }

    public IntegerProperty paginasProperty() {
        return paginas;
    }
}
