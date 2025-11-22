package com.diagonal.cordeis.util;

public class ColecaoUtils {
    
    private static final String PREFIXO_COLECAO = "COL";

    /**
     * Gera o próximo SKU para coleção baseado no último número usado
     * @param ultimoNumero último número usado (ex: 1 para COL0001)
     * @return novo SKU (ex: COL0002)
     */
    public static String gerarProximoSku(int ultimoNumero) {
        return String.format("%s%04d", PREFIXO_COLECAO, ultimoNumero + 1);
    }

    /**
     * Extrai o número do SKU de uma coleção
     * @param sku SKU da coleção (ex: COL0001)
     * @return número extraído (ex: 1) ou 0 se inválido
     */
    public static int extrairNumeroDeSku(String sku) {
        if (sku == null || !sku.startsWith(PREFIXO_COLECAO) || sku.length() != 7) {
            return 0;
        }
        
        try {
            return Integer.parseInt(sku.substring(3));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Valida se um SKU de coleção está no formato correto
     * @param sku SKU a validar
     * @return true se válido
     */
    public static boolean isSkuValido(String sku) {
        return sku != null && 
               sku.matches("^COL\\d{4}$") && 
               extrairNumeroDeSku(sku) > 0;
    }
}
