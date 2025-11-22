package com.diagonal.cordeis.util;

import com.diagonal.cordeis.model.TipoLivro;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LivroUtilsTest {

    @Test
    void testExtrairSkuMiniCordel() {
        assertEquals("MC0001", LivroUtils.extrairSku("MC0001_Historia_do_Lampiao.pdf"));
        assertEquals("MC9999", LivroUtils.extrairSku("mc9999-titulo-do-livro.pdf"));
        assertEquals("MC1234", LivroUtils.extrairSku("Algum_MC1234_Texto.pdf"));
    }

    @Test
    void testExtrairSkuFolhetoCordel() {
        assertEquals("FC0001", LivroUtils.extrairSku("FC0001_Historia_do_Lampiao.pdf"));
        assertEquals("FC9999", LivroUtils.extrairSku("fc9999-titulo-do-livro.pdf"));
        assertEquals("FC1234", LivroUtils.extrairSku("Algum_FC1234_Texto.pdf"));
    }

    @Test
    void testExtrairSkuSemSku() {
        assertNull(LivroUtils.extrairSku("historia_do_lampiao.pdf"));
        assertNull(LivroUtils.extrairSku("FC_sem_numero.pdf"));
        assertNull(LivroUtils.extrairSku("MC_sem_numero.pdf"));
        assertNull(LivroUtils.extrairSku("arquivo_sem_sku.pdf"));
    }

    @Test
    void testDeterminarTipoMini() {
        assertEquals(TipoLivro.MINI, LivroUtils.determinarTipo("MC0001"));
        assertEquals(TipoLivro.MINI, LivroUtils.determinarTipo("mc1234"));
        assertEquals(TipoLivro.MINI, LivroUtils.determinarTipo("MC9999"));
    }

    @Test
    void testDeterminarTipoNormal() {
        assertEquals(TipoLivro.NORMAL, LivroUtils.determinarTipo("FC0001"));
        assertEquals(TipoLivro.NORMAL, LivroUtils.determinarTipo("fc1234"));
        assertEquals(TipoLivro.NORMAL, LivroUtils.determinarTipo("FC9999"));
        assertEquals(TipoLivro.NORMAL, LivroUtils.determinarTipo(""));
        assertEquals(TipoLivro.NORMAL, LivroUtils.determinarTipo(null));
        assertEquals(TipoLivro.NORMAL, LivroUtils.determinarTipo("INVALIDO"));
    }

    @Test
    void testExtrairTitulo() {
        assertEquals("Historia Do Lampiao", LivroUtils.extrairTitulo("MC0001_historia_do_lampiao.pdf"));
        assertEquals("Cordel Do Norte", LivroUtils.extrairTitulo("FC9999-cordel-do-norte.pdf"));
        assertEquals("Titulo Simples", LivroUtils.extrairTitulo("titulo_simples.pdf"));
        assertEquals("Multiplas Palavras No Titulo", LivroUtils.extrairTitulo("FC1234_multiplas_palavras_no_titulo.pdf"));
    }

    @Test
    void testValidarSku() {
        assertTrue(LivroUtils.validarSku("MC0001"));
        assertTrue(LivroUtils.validarSku("FC9999"));
        assertTrue(LivroUtils.validarSku("mc1234"));
        assertTrue(LivroUtils.validarSku("fc5678"));
        
        assertFalse(LivroUtils.validarSku("MC123"));   // menos de 4 dígitos
        assertFalse(LivroUtils.validarSku("MC12345")); // mais de 4 dígitos
        assertFalse(LivroUtils.validarSku("XX0001"));  // prefixo inválido
        assertFalse(LivroUtils.validarSku(""));
        assertFalse(LivroUtils.validarSku(null));
    }

    @Test
    void testGerarProximoSku() {
        assertEquals("MC0001", LivroUtils.gerarProximoSku(TipoLivro.MINI, 0));
        assertEquals("MC0002", LivroUtils.gerarProximoSku(TipoLivro.MINI, 1));
        assertEquals("MC9999", LivroUtils.gerarProximoSku(TipoLivro.MINI, 9998));
        
        assertEquals("FC0001", LivroUtils.gerarProximoSku(TipoLivro.NORMAL, 0));
        assertEquals("FC0002", LivroUtils.gerarProximoSku(TipoLivro.NORMAL, 1));
        assertEquals("FC9999", LivroUtils.gerarProximoSku(TipoLivro.NORMAL, 9998));
    }
}
