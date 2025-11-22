package com.diagonal.cordeis.repository;

import com.diagonal.cordeis.model.Livro;
import com.diagonal.cordeis.model.TipoLivro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    Optional<Livro> findByTituloAndTipo(String titulo, TipoLivro tipo);

    Optional<Livro> findBySku(String sku);

    @Query("SELECT l FROM Livro l WHERE l.sku LIKE :prefixo% ORDER BY l.sku DESC")
    List<Livro> findBySkuStartingWithOrderBySkuDesc(@Param("prefixo") String prefixo);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(l.sku, LENGTH(:prefixo) + 1) AS long)), 0) FROM Livro l WHERE l.sku LIKE CONCAT(:prefixo, '%')")
    Long findUltimoNumeroBySku(@Param("prefixo") String prefixo);

    @Query("SELECT COUNT(l) FROM Livro l")
    Long contarTodos();
}