package com.diagonal.cordeis.service;

import com.diagonal.cordeis.model.*;
import com.diagonal.cordeis.repository.ImpressaoRepository;
import com.diagonal.cordeis.security.AuthenticationService;
import com.diagonal.cordeis.util.ArquivoUtils;
import com.diagonal.cordeis.util.ImpressoraUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImpressaoService {

    private final ImpressaoRepository impressaoRepository;
    private final AuthenticationService authService;

    /**
     * Imprime um livro completo.
     * O PDF do livro já possui capa integrada e é impresso como um documento único.
     */
    public void imprimir(Livro livro, String impressora) throws RuntimeException {
        // Check printing permission
        authService.checkPermission(Permission.PRINT_BOOKS);

        StringBuilder obs = new StringBuilder();

        try {
            // Validar PDF do livro
            if (!ArquivoUtils.validarPdf(livro.getCaminhoPdf())) {
                throw new IllegalArgumentException("PDF do livro inválido: " + livro.getCaminhoPdf());
            }

            // Imprimir livro completo
            ImpressoraUtil.imprimirPdf(livro.getCaminhoPdf(), impressora);
            obs.append("Livro impresso com sucesso.");

            // Registrar impressão bem-sucedida
            registrarImpressao(livro, impressora, StatusImpressao.SUCESSO, obs.toString());

        } catch (Exception e) {
            obs.append("Erro ao imprimir livro: ").append(e.getMessage());
            registrarImpressao(livro, impressora, StatusImpressao.FALHA, obs.toString());
            throw new RuntimeException("Erro durante impressão: " + e.getMessage(), e);
        }
    }

    /**
     * Imprime todos os livros de uma coleção.
     * Cada livro é impresso individualmente e o resultado é registrado.
     */
    public ImpressaoColecaoResult imprimirColecao(Colecao colecao, String impressora) {
        // Check printing permission
        authService.checkPermission(Permission.PRINT_BOOKS);

        if (colecao == null || colecao.getLivros().isEmpty()) {
            log.warn("Nenhuma coleção ou livros encontrados para impressão.");
            return new ImpressaoColecaoResult(new ArrayList<>(), new ArrayList<>(), 0);
        }

        List<Livro> livros = new ArrayList<>(colecao.getLivros());
        log.info("Iniciando impressão da coleção: {} com {} livros", colecao.getNome(), livros.size());

        List<Livro> livrosComSucesso = new ArrayList<>();
        List<String> erros = new ArrayList<>();

        for (Livro livro : livros) {
            try {
                log.debug("Imprimindo livro: {} da coleção: {}", livro.getTitulo(), colecao.getNome());
                imprimir(livro, impressora);
                livrosComSucesso.add(livro);
                log.debug("Livro impresso com sucesso: {}", livro.getTitulo());
            } catch (Exception e) {
                String erro = String.format("Erro ao imprimir '%s': %s", livro.getTitulo(), e.getMessage());
                erros.add(erro);
                log.error("Erro ao imprimir livro {} da coleção {}: {}", livro.getTitulo(), colecao.getNome(), e.getMessage());
            }
        }

        log.info("Impressão da coleção finalizada. Sucessos: {}, Erros: {}", livrosComSucesso.size(), erros.size());
        return new ImpressaoColecaoResult(livrosComSucesso, erros, livros.size());
    }

    /**
     * Registra uma impressão no histórico.
     */
    private void registrarImpressao(Livro livro, String impressora, StatusImpressao status, String observacoes) {
        Impressao impressao = new Impressao();
        impressao.setLivro(livro);
        impressao.setDataHora(LocalDateTime.now());
        impressao.setImpressora(impressora);
        impressao.setStatus(status);
        impressao.setObservacoes(observacoes);
        impressao.setUsuario(authService.getCurrentUser()); // Track who did the printing
        impressaoRepository.save(impressao);
    }

    /**
     * Lista o histórico de impressões.
     * Requer permissão para gerenciar impressões.
     */
    public List<Impressao> listarHistoricoImpressoes() {
        // Check permission for viewing print history
        authService.checkPermission(Permission.PRINT_BOOKS);
        return impressaoRepository.findAllByOrderByDataHoraDesc();
    }

    /**
     * Classe para encapsular o resultado da impressão de uma coleção.
     */
    public static class ImpressaoColecaoResult {
        private final List<Livro> livrosImpressos;
        private final List<String> erros;
        private final int totalLivros;

        public ImpressaoColecaoResult(List<Livro> livrosImpressos, List<String> erros, int totalLivros) {
            this.livrosImpressos = livrosImpressos;
            this.erros = erros;
            this.totalLivros = totalLivros;
        }

        public List<Livro> getLivrosImpressos() {
            return livrosImpressos;
        }

        public List<String> getErros() {
            return erros;
        }

        public int getTotalLivros() {
            return totalLivros;
        }

        public int getQuantidadeSucesso() {
            return livrosImpressos.size();
        }

        public int getQuantidadeErros() {
            return erros.size();
        }

        public boolean tudoOk() {
            return erros.isEmpty() && !livrosImpressos.isEmpty();
        }
    }
}