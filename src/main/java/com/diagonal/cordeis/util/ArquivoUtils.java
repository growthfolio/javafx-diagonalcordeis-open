package com.diagonal.cordeis.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ArquivoUtils {

    /**
     * Extrai apenas o nome do arquivo a partir de um caminho completo.
     * Ex: "/home/felipe/livros/livroA.pdf" → "livroA.pdf"
     */
    public static String extrairNomeArquivo(String caminhoCompleto) {
        if (caminhoCompleto == null || caminhoCompleto.isBlank()) {
            log.warn("extrairNomeArquivo: caminho vazio ou nulo");
            return "";
        }

        String nome = Paths.get(caminhoCompleto).getFileName().toString();
        log.info("Nome do arquivo extraído: {}", nome);
        return nome;
    }

    /**
     * Conta a quantidade de páginas de um PDF usando PDFBox.
     * Lança RuntimeException se o arquivo não for válido ou acessível.
     */
    public static int contarPaginasPdf(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            log.error("contarPaginasPdf: Caminho do PDF está vazio ou nulo");
            throw new IllegalArgumentException("Caminho do PDF inválido." + caminho);
        }

        Path path = Paths.get(caminho);
        if (!Files.exists(path)) {
            log.error("contarPaginasPdf: Arquivo não encontrado: {}", caminho);
            throw new RuntimeException("Arquivo não encontrado: " + caminho);
        }

        try (PDDocument document = PDDocument.load(path.toFile())) {
            int paginas = document.getNumberOfPages();
            log.info("PDF '{}' possui {} página(s)", caminho, paginas);
            return paginas;
        } catch (IOException e) {
            log.error("Erro ao abrir PDF '{}': {}", caminho, e.getMessage());
            throw new RuntimeException("Erro ao processar PDF: " + caminho, e);
        }
    }

    /**
     * Verifica se o arquivo no caminho informado realmente existe.
     */
    public static boolean arquivoExiste(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            log.warn("arquivoExiste: Caminho vazio ou nulo");
            return false;
        }
        return Files.exists(Paths.get(caminho));
    }

    /**
     * Valida se o caminho é um PDF acessível e não corrompido.
     */
    public static boolean validarPdf(String caminhoPdf) {
        if (caminhoPdf == null || caminhoPdf.isBlank()) {
            log.warn("validarPdf: Caminho vazio ou nulo");
            return false;
        }

        try (PDDocument doc = PDDocument.load(new File(caminhoPdf))) {
            boolean valido = doc.getNumberOfPages() > 0;
            log.info("validarPdf: PDF '{}' é válido? {}", caminhoPdf, valido);
            return valido;
        } catch (IOException e) {
            log.warn("validarPdf: PDF inválido '{}': {}", caminhoPdf, e.getMessage());
            return false;
        }
    }

    @NotNull
    public static List<Path> listarPdfsEmDiretorio() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Selecione o diretório contendo os arquivos PDF");

        int resultado = chooser.showOpenDialog(null);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            log.info("Nenhum diretório selecionado.");
            return List.of();
        }

        File diretorio = chooser.getSelectedFile();
        log.info("Diretório selecionado: {}", diretorio.getAbsolutePath());

        List<Path> arquivosValidos = new ArrayList<>();
        try {
            arquivosValidos = Files.list(diretorio.toPath())
                    .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
                    .filter(path -> validarPdf(path.toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Erro ao listar arquivos no diretório '{}': {}", diretorio.getAbsolutePath(), e.getMessage());
        }

        log.info("PDFs válidos encontrados: {}", arquivosValidos.size());
        return arquivosValidos;
    }

    /**
     * Extrai páginas específicas de um PDF e salva como um novo arquivo temporário.
     *
     * @param caminhoOriginal Caminho do PDF original
     * @param paginaInicio   Página inicial (1-based)
     * @param paginaFim      Página final (1-based, inclusive)
     * @param sufixo         Sufixo para o nome do arquivo temporário (ex: "_capa", "_conteudo")
     * @return Caminho do arquivo temporário criado
     */
    public static String extrairPaginasPdf(String caminhoOriginal, int paginaInicio, int paginaFim, String sufixo) throws IOException {
        if (caminhoOriginal == null || caminhoOriginal.isBlank()) {
            throw new IllegalArgumentException("Caminho do PDF original inválido");
        }

        Path pathOriginal = Paths.get(caminhoOriginal);
        if (!Files.exists(pathOriginal)) {
            throw new RuntimeException("Arquivo original não encontrado: " + caminhoOriginal);
        }

        // Criar nome do arquivo temporário
        String nomeArquivo = pathOriginal.getFileName().toString();
        String nomeBase = nomeArquivo.replace(".pdf", "");
        String nomeTemp = nomeBase + sufixo + ".pdf";
        Path pathTemp = pathOriginal.getParent().resolve("temp_" + nomeTemp);

        try (PDDocument documentoOriginal = PDDocument.load(pathOriginal.toFile());
             PDDocument documentoDestino = new PDDocument()) {

            int totalPaginas = documentoOriginal.getNumberOfPages();

            // Validar páginas
            if (paginaInicio < 1 || paginaInicio > totalPaginas) {
                throw new IllegalArgumentException("Página inicial inválida: " + paginaInicio);
            }
            if (paginaFim < paginaInicio || paginaFim > totalPaginas) {
                throw new IllegalArgumentException("Página final inválida: " + paginaFim);
            }

            // Extrair páginas (convertendo de 1-based para 0-based)
            for (int i = paginaInicio - 1; i < paginaFim; i++) {
                documentoDestino.addPage(documentoOriginal.getPage(i));
            }

            documentoDestino.save(pathTemp.toFile());
            log.info("PDF extraído com sucesso: {} (páginas {}-{})", pathTemp, paginaInicio, paginaFim);

            return pathTemp.toString();

        } catch (IOException e) {
            log.error("Erro ao extrair páginas do PDF '{}': {}", caminhoOriginal, e.getMessage());
            throw new IOException("Erro ao processar PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Remove arquivos temporários criados durante o processo de impressão
     */
    public static void limparArquivosTemporarios(String diretorio) {
        try {
            Path pathDiretorio = Paths.get(diretorio);
            if (Files.exists(pathDiretorio)) {
                Files.list(pathDiretorio)
                        .filter(path -> path.getFileName().toString().startsWith("temp_"))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                log.info("Arquivo temporário removido: {}", path);
                            } catch (IOException e) {
                                log.warn("Não foi possível remover arquivo temporário: {}", path);
                            }
                        });
            }
        } catch (IOException e) {
            log.warn("Erro ao limpar arquivos temporários: {}", e.getMessage());
        }
    }
}
