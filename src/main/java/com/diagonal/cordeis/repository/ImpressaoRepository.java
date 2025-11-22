package com.diagonal.cordeis.repository;

import com.diagonal.cordeis.model.Impressao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImpressaoRepository extends JpaRepository<Impressao, Long> {
    /**
     * Retorna todas as impressões ordenadas por data/hora decrescente (mais recentes primeiro)
     */
    List<Impressao> findAllByOrderByDataHoraDesc();
    /**
     * Busca impressões por status
     */
    List<Impressao> findByStatus(com.diagonal.cordeis.model.StatusImpressao status);
    /**
     * Busca impressões por usuário
     */
    List<Impressao> findByUsuarioId(Long usuarioId);
}