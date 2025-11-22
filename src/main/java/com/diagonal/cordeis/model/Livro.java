package com.diagonal.cordeis.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "livro",
        uniqueConstraints = @UniqueConstraint(columnNames = {"titulo", "tipo"})
)
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(10) default 'NORMAL'")
    @Enumerated(EnumType.STRING)
    private TipoLivro tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(unique = true, length = 10)
    private String sku;

    private String nomeArquivo;
    private String caminhoPdf;
    private int paginas;

    @ManyToMany(mappedBy = "livros")
    private Set<Colecao> colecoes = new HashSet<>();

    @Override
    public String toString() {
        return "Livro{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", titulo='" + titulo + '\'' +
                ", tipo=" + tipo +
                ", nomeArquivo='" + nomeArquivo + '\'' +
                '}';
    }
}