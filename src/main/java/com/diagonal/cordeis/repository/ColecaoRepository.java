package com.diagonal.cordeis.repository;

import com.diagonal.cordeis.model.Colecao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ColecaoRepository extends JpaRepository<Colecao, Long> {
    
    Optional<Colecao> findBySku(String sku);
    
    List<Colecao> findByNomeContainingIgnoreCase(String nome);
    
    @Query("SELECT c FROM Colecao c WHERE c.sku LIKE :prefixo% ORDER BY c.sku DESC")
    List<Colecao> findBySkuStartingWithOrderBySkuDesc(@Param("prefixo") String prefixo);
    
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Impressao i " +
           "JOIN i.livro l JOIN Colecao c JOIN c.livros cl WHERE cl.id = l.id AND c.id = :colecaoId")
    boolean existsImpressaoByColecaoId(@Param("colecaoId") Long colecaoId);
}
