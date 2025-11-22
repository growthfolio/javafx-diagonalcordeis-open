package com.diagonal.cordeis.dto;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PdfItemDTO {
    private final StringProperty caminho = new SimpleStringProperty();
    private final StringProperty titulo = new SimpleStringProperty();
    private final StringProperty tipo = new SimpleStringProperty();
    private int paginas;

    public PdfItemDTO(String caminho, String titulo, String tipo) {
        this.caminho.set(caminho);
        this.titulo.set(titulo);
        this.tipo.set(tipo);
        this.paginas = 0; // Inicializa com 0, será definido posteriormente
    }

    public StringProperty caminhoProperty() { return caminho; }
    public StringProperty tituloProperty() { return titulo; }
    public StringProperty tipoProperty() { return tipo; }

    // GETTERS tradicionais
    public String getCaminho() { return caminho.get(); }
    public String getTitulo() { return titulo.get(); }
    public String getTipo() { return tipo.get(); }

    public int getPaginas() { return paginas; }

    // SETTERS se quiser editar os valores também
    public void setCaminho(String caminho) { this.caminho.set(caminho); }
    public void setTitulo(String titulo) { this.titulo.set(titulo); }
    public void setTipo(String tipo) { this.tipo.set(tipo); }
    public void setPaginas(int paginas) { this.paginas = paginas; }
}
