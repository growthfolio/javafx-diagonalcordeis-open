package com.diagonal.cordeis.service;

import com.diagonal.cordeis.dto.ColecaoDTO;
import com.diagonal.cordeis.dto.ColecaoRequestDTO;
import com.diagonal.cordeis.dto.ColecaoResponseDTO;
import com.diagonal.cordeis.model.Colecao;
import com.diagonal.cordeis.model.Livro;
import com.diagonal.cordeis.model.TipoLivro;
import com.diagonal.cordeis.repository.ColecaoRepository;
import com.diagonal.cordeis.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColecaoServiceTest {

    @Mock
    private ColecaoRepository colecaoRepository;

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private ColecaoService colecaoService;

    private Livro livro1;
    private Livro livro2;
    private ColecaoRequestDTO request;

    @BeforeEach
    void setUp() {
        livro1 = new Livro();
        livro1.setId(1L);
        livro1.setTitulo("Cordel do Lampião");
        livro1.setSku("FC0001");
        livro1.setTipo(TipoLivro.NORMAL);

        livro2 = new Livro();
        livro2.setId(2L);
        livro2.setTitulo("Maria Bonita");
        livro2.setSku("FC0002");
        livro2.setTipo(TipoLivro.NORMAL);

        request = new ColecaoRequestDTO();
        request.setNome("Coleção Lampião");
        request.setSku("COL001");
        request.setIdsLivros(Arrays.asList(1L, 2L));
    }

    @Test
    void deveCriarColecaoComSucesso() {
        // Given
        when(colecaoRepository.findBySku(anyString())).thenReturn(Optional.empty());
        when(livroRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(livro1, livro2));
        
        Colecao colecaoSalva = new Colecao("COL001", "Coleção Lampião");
        colecaoSalva.setId(1L);
        colecaoSalva.addLivro(livro1);
        colecaoSalva.addLivro(livro2);
        
        when(colecaoRepository.save(any(Colecao.class))).thenReturn(colecaoSalva);

        // When
        ColecaoResponseDTO resultado = colecaoService.criarColecao(request);

        // Then
        assertNotNull(resultado);
        assertEquals("Coleção Lampião", resultado.getNome());
        assertEquals("COL001", resultado.getSku());
        assertEquals(2, resultado.getQuantidadeLivros());
        assertTrue(resultado.getNomesLivrosTexto().contains("Cordel do Lampião"));
        assertTrue(resultado.getNomesLivrosTexto().contains("Maria Bonita"));

        verify(colecaoRepository).findBySku("COL001");
        verify(livroRepository).findAllById(Arrays.asList(1L, 2L));
        verify(colecaoRepository).save(any(Colecao.class));
    }

    @Test
    void deveGerarProximoSkuCorretamente() {
        // Given
        Colecao colecao1 = new Colecao("COL003", "Teste");
        when(colecaoRepository.findBySkuStartingWithOrderBySkuDesc("COL"))
                .thenReturn(Arrays.asList(colecao1));

        // When
        String novoSku = colecaoService.gerarProximoSku();

        // Then
        assertEquals("COL0004", novoSku);
    }

    @Test
    void deveLancarExcecaoQuandoSkuJaExiste() {
        // Given
        Colecao colecaoExistente = new Colecao("COL001", "Existente");
        when(colecaoRepository.findBySku("COL001")).thenReturn(Optional.of(colecaoExistente));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            colecaoService.criarColecao(request);
        });

        verify(colecaoRepository).findBySku("COL001");
        verify(colecaoRepository, never()).save(any(Colecao.class));
    }

    @Test
    void deveListarTodasAsColecoes() {
        // Given
        Colecao colecao1 = new Colecao("COL001", "Coleção 1");
        colecao1.setId(1L);
        Colecao colecao2 = new Colecao("COL002", "Coleção 2");
        colecao2.setId(2L);

        when(colecaoRepository.findAll()).thenReturn(Arrays.asList(colecao1, colecao2));

        // When
        List<ColecaoDTO> resultado = colecaoService.listarTodas();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("Coleção 1", resultado.get(0).getNome());
        assertEquals("Coleção 2", resultado.get(1).getNome());
    }
}
