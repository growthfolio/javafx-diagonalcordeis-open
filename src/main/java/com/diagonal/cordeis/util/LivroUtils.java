package com.diagonal.cordeis.util;

import com.diagonal.cordeis.model.TipoLivro;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class LivroUtils {

    // Padrões para identificar o tipo e SKU do livro
    private static final Pattern PATTERN_MINI = Pattern.compile("MC(\\d{4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_NORMAL = Pattern.compile("FC(\\d{4})", Pattern.CASE_INSENSITIVE);

    /**
     * Extrai o SKU do nome do arquivo.
     * Formatos esperados: MC0001, FC0001, etc.
     */
    public static String extrairSku(String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            return null;
        }

        // Remove extensão se houver
        String nomeLimpo = nomeArquivo.replaceAll("\\.[^.]+$", "");

        // Busca por padrão MC#### (Mini Cordel)
        Matcher matcherMini = PATTERN_MINI.matcher(nomeLimpo);
        if (matcherMini.find()) {
            return "MC" + matcherMini.group(1);
        }

        // Busca por padrão FC#### (Folheto Cordel)
        Matcher matcherNormal = PATTERN_NORMAL.matcher(nomeLimpo);
        if (matcherNormal.find()) {
            return "FC" + matcherNormal.group(1);
        }

        log.debug("SKU não encontrado no nome do arquivo: {}", nomeArquivo);
        return null;
    }

    /**
     * Determina o tipo do livro baseado no SKU.
     */
    public static TipoLivro determinarTipo(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return TipoLivro.NORMAL; // padrão
        }

        String skuUpper = sku.toUpperCase();
        if (skuUpper.startsWith("MC")) {
            return TipoLivro.MINI;
        } else if (skuUpper.startsWith("FC")) {
            return TipoLivro.NORMAL;
        }

        return TipoLivro.NORMAL; // padrão
    }

    /**
     * Determina o tipo do livro baseado no nome do arquivo.
     */
    public static TipoLivro determinarTipoPorNome(String nomeArquivo) {
        String sku = extrairSku(nomeArquivo);
        return determinarTipo(sku);
    }

    /**
     * Extrai o título do livro removendo o SKU e limpando o nome.
     */
    public static String extrairTitulo(String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            return "";
        }

        // Remove extensão
        String nomeLimpo = nomeArquivo.replaceAll("\\.[^.]+$", "");

        // Remove SKU (MC#### ou FC####)
        nomeLimpo = nomeLimpo.replaceAll("(?i)(MC|FC)\\d{4}", "").trim();

        // Remove caracteres especiais e limpa espaços extras
        nomeLimpo = nomeLimpo.replaceAll("[_\\-]+", " ");
        nomeLimpo = nomeLimpo.replaceAll("\\s+", " ");
        nomeLimpo = nomeLimpo.trim();

        // Capitaliza primeira letra de cada palavra
        return capitalizarTitulo(nomeLimpo);
    }

    /**
     * Capitaliza o título (primeira letra de cada palavra maiúscula).
     */
    private static String capitalizarTitulo(String titulo) {
        if (titulo == null || titulo.isEmpty()) {
            return titulo;
        }

        String[] palavras = titulo.toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palavra : palavras) {
            if (!palavra.isEmpty()) {
                if (resultado.length() > 0) {
                    resultado.append(" ");
                }
                resultado.append(Character.toUpperCase(palavra.charAt(0)));
                if (palavra.length() > 1) {
                    resultado.append(palavra.substring(1));
                }
            }
        }

        return resultado.toString();
    }

    /**
     * Valida se o SKU está no formato correto.
     */
    public static boolean validarSku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return false;
        }

        return sku.matches("(?i)(MC|FC)\\d{4}");
    }

    /**
     * Gera o próximo SKU baseado no tipo e na numeração existente.
     */
    public static String gerarProximoSku(TipoLivro tipo, int ultimoNumero) {
        String prefixo = (tipo == TipoLivro.MINI) ? "MC" : "FC";
        return String.format("%s%04d", prefixo, ultimoNumero + 1);
    }
}
