package com.diagonal.cordeis.dto;

import com.diagonal.cordeis.model.Livro;
import com.diagonal.cordeis.model.TipoLivro;
import javafx.beans.property.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class LivroDTO {

    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty titulo = new SimpleStringProperty();
    private final StringProperty sku = new SimpleStringProperty();
    private final StringProperty nomeArquivo = new SimpleStringProperty();
    private final StringProperty caminhoPdf = new SimpleStringProperty();
    private final IntegerProperty paginas = new SimpleIntegerProperty();
    private final ObjectProperty<TipoLivro> tipo = new SimpleObjectProperty<>();

    // Propriedades JavaFX
    public LongProperty idProperty() { return id; }
    public StringProperty tituloProperty() { return titulo; }
    public StringProperty skuProperty() { return sku; }
    public StringProperty nomeArquivoProperty() { return nomeArquivo; }
    public StringProperty caminhoPdfProperty() { return caminhoPdf; }
    public IntegerProperty paginasProperty() { return paginas; }
    public ObjectProperty<TipoLivro> tipoProperty() { return tipo; }

    // Getters e Setters
    public long getId() { return id.get(); }
    public void setId(long id) { this.id.set(id); }

    public String getTitulo() { return titulo.get(); }
    public void setTitulo(String titulo) { this.titulo.set(titulo); }

    public String getSku() { return sku.get(); }
    public void setSku(String sku) { this.sku.set(sku); }

    public String getNomeArquivo() { return nomeArquivo.get(); }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo.set(nomeArquivo); }

    public String getCaminhoPdf() { return caminhoPdf.get(); }
    public void setCaminhoPdf(String caminhoPdf) { this.caminhoPdf.set(caminhoPdf); }

    public int getPaginas() { return paginas.get(); }
    public void setPaginas(int paginas) { this.paginas.set(paginas); }

    public TipoLivro getTipo() { return tipo.get(); }
    public void setTipo(TipoLivro tipo) { this.tipo.set(tipo); }

    // Conversão: Entidade -> DTO
    public static LivroDTO fromModel(Livro livro) {
        LivroDTO dto = new LivroDTO();
        dto.setId(livro.getId() != null ? livro.getId() : 0L);
        dto.setTitulo(livro.getTitulo());
        dto.setSku(livro.getSku());
        dto.setTipo(livro.getTipo()); // ✅ Adicionado
        dto.setNomeArquivo(livro.getNomeArquivo());
        dto.setCaminhoPdf(livro.getCaminhoPdf());
        dto.setPaginas(livro.getPaginas());

        return dto;
    }

    // Conversão: DTO -> Entidade
    public Livro toModel() {
        Livro livro = new Livro();
        livro.setId(getId());
        livro.setTitulo(getTitulo());
        livro.setSku(getSku());
        livro.setTipo(getTipo()); // ✅ Adicionado
        livro.setNomeArquivo(getNomeArquivo());
        livro.setCaminhoPdf(getCaminhoPdf());
        livro.setPaginas(getPaginas());

        return livro;
    }

    @Override
    public String toString() {
        return titulo.get() + " " + getTipo();
    }
}

