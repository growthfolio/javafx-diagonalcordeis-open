package com.diagonal.cordeis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "colecao")
public class Colecao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String sku;

    @Column(nullable = false)
    private String nome;

    @ManyToMany
    @JoinTable(
        name = "colecao_livro",
        joinColumns = @JoinColumn(name = "colecao_id"),
        inverseJoinColumns = @JoinColumn(name = "livro_id")
    )
    private Set<Livro> livros = new HashSet<>();

    public Colecao(String sku, String nome) {
        this.sku = sku;
        this.nome = nome;
        this.livros = new HashSet<>();
    }

    public void addLivro(Livro livro) {
        this.livros.add(livro);
        livro.getColecoes().add(this);
    }

    public void removeLivro(Livro livro) {
        this.livros.remove(livro);
        livro.getColecoes().remove(this);
    }

    public int getQuantidadeLivros() {
        return livros != null ? livros.size() : 0;
    }
}
