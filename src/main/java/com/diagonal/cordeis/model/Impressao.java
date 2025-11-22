package com.diagonal.cordeis.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Impressao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    private Livro livro;

    @ManyToOne
    private User usuario;

    private String impressora;
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    private StatusImpressao status;

    @Column(length = 1000)
    private String observacoes;
}