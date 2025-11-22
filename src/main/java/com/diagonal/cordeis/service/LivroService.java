package com.diagonal.cordeis.service;

import com.diagonal.cordeis.dto.LivroDTO;
import com.diagonal.cordeis.model.Livro;
import com.diagonal.cordeis.model.Permission;
import com.diagonal.cordeis.model.TipoLivro;
import com.diagonal.cordeis.repository.LivroRepository;
import com.diagonal.cordeis.security.AuthenticationService;
import com.diagonal.cordeis.util.ArquivoUtils;
import com.diagonal.cordeis.util.LivroUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository livroRepository;
    private final AuthenticationService authService;

    @Transactional
    public LivroDTO salvar(LivroDTO dto) throws IOException {
        // Check permission before any operation
        authService.checkPermission(Permission.MANAGE_BOOKS);
        
        log.info("[INFO] Iniciando salvamento do livro: '{}'", dto.getTitulo());

        try {
            Livro livro = dto.toModel();
            String caminho = livro.getCaminhoPdf();

            if (caminho == null || caminho.isBlank()) {
                log.error("[ERROR] Caminho do PDF está nulo ou em branco.");
                throw new IOException("Caminho do PDF inválido.");
            }

            if (!ArquivoUtils.arquivoExiste(caminho)) {
                log.error("❌ Arquivo PDF não encontrado: {}", caminho);
                throw new IOException("Arquivo PDF não encontrado: " + caminho);
            }

            livro.setNomeArquivo(ArquivoUtils.extrairNomeArquivo(caminho));
            livro.setPaginas(ArquivoUtils.contarPaginasPdf(caminho));

            // Extrair informações automáticas do nome do arquivo
            String nomeArquivo = livro.getNomeArquivo();
            
            // Extrair SKU do nome do arquivo
            String skuExtraido = LivroUtils.extrairSku(nomeArquivo);
            if (skuExtraido != null && !skuExtraido.isEmpty()) {
                // Verificar se SKU já existe
                Optional<Livro> livroExistente = livroRepository.findBySku(skuExtraido);
                if (livroExistente.isPresent() && !livroExistente.get().getId().equals(livro.getId())) {
                    throw new IOException("SKU '" + skuExtraido + "' já está em uso por outro livro.");
                }
                livro.setSku(skuExtraido);
                log.info("✅ SKU extraído do arquivo: {}", skuExtraido);
            } else {
                // Gerar SKU automático se não foi possível extrair
                TipoLivro tipoDetectado = LivroUtils.determinarTipoPorNome(nomeArquivo);
                livro.setTipo(tipoDetectado);
                
                String novoSku = gerarProximoSku(tipoDetectado);
                livro.setSku(novoSku);
                log.info("✅ SKU gerado automaticamente: {}", novoSku);
            }

            // Determinar tipo automaticamente baseado no SKU
            TipoLivro tipoAutomatico = LivroUtils.determinarTipo(livro.getSku());
            livro.setTipo(tipoAutomatico);
            
            // Extrair título limpo se não foi fornecido ou se deve ser atualizado
            if (livro.getTitulo() == null || livro.getTitulo().trim().isEmpty()) {
                String tituloExtraido = LivroUtils.extrairTitulo(nomeArquivo);
                if (!tituloExtraido.isEmpty()) {
                    livro.setTitulo(tituloExtraido);
                    log.info("✅ Título extraído do arquivo: {}", tituloExtraido);
                }
            }

            Livro salvo = livroRepository.save(livro);
            log.info("✅ Livro salvo com sucesso: '{}' (SKU: {})", salvo.getTitulo(), salvo.getSku());
            return LivroDTO.fromModel(salvo);

        } catch (Exception e) {
            log.error("🔥 Erro ao salvar livro '{}': {}", dto.getTitulo(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public LivroDTO atualizar(LivroDTO dto) throws IOException {
        // Check permission before any operation
        authService.checkPermission(Permission.MANAGE_BOOKS);
        return salvar(dto); // reutiliza a lógica do salvar
    }

    public List<LivroDTO> listarTodos() {
        // Require at least view permission
        if (!authService.hasPermission(Permission.VIEW_BOOKS) && 
            !authService.hasPermission(Permission.MANAGE_BOOKS)) {
            throw new AccessDeniedException("Você não tem permissão para visualizar livros.");
        }
        
        return livroRepository.findAll()
                .stream()
                .map(LivroDTO::fromModel)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void excluir(Long id) {
        // Check permission before deletion
        authService.checkPermission(Permission.MANAGE_BOOKS);
        
        livroRepository.findById(id)
            .ifPresentOrElse(
                livro -> {
                    livroRepository.delete(livro);
                    log.info("✅ Livro excluído com sucesso: '{}'", livro.getTitulo());
                },
                () -> {
                    throw new IllegalArgumentException("Livro não encontrado com ID: " + id);
                }
            );
    }
    
    public LivroDTO buscarPorId(Long id) {
        // Require at least view permission
        if (!authService.hasPermission(Permission.VIEW_BOOKS) && 
            !authService.hasPermission(Permission.MANAGE_BOOKS)) {
            throw new AccessDeniedException("Você não tem permissão para visualizar livros.");
        }
        
        return livroRepository.findById(id)
            .map(LivroDTO::fromModel)
            .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado com ID: " + id));
    }
    
    public LivroDTO buscarPorSku(String sku) {
        // Require at least view permission
        if (!authService.hasPermission(Permission.VIEW_BOOKS) && 
            !authService.hasPermission(Permission.MANAGE_BOOKS)) {
            throw new AccessDeniedException("Você não tem permissão para visualizar livros.");
        }
        
        return livroRepository.findBySku(sku)
            .map(LivroDTO::fromModel)
            .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado com SKU: " + sku));
    }
    
    @Transactional
    public LivroDTO atualizarTitulo(Long id, String novoTitulo) {
        // Check permission before update
        authService.checkPermission(Permission.MANAGE_BOOKS);
        
        Livro livro = livroRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado com ID: " + id));
            
        livro.setTitulo(novoTitulo);
        Livro atualizado = livroRepository.save(livro);
        log.info("✅ Título do livro atualizado: '{}' (SKU: {})", atualizado.getTitulo(), atualizado.getSku());
        
        return LivroDTO.fromModel(atualizado);
    }
    
    @Transactional
    public LivroDTO atualizarSku(long id, String novoSku) {
        // Check permission before update
        authService.checkPermission(Permission.MANAGE_BOOKS);
        
        Livro livro = livroRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado com ID: " + id));
            
        // Verificar se o SKU já existe
        if (livroRepository.findBySku(novoSku).isPresent()) {
            throw new IllegalArgumentException("SKU '" + novoSku + "' já está em uso por outro livro.");
        }
            
        livro.setSku(novoSku);
        Livro atualizado = livroRepository.save(livro);
        log.info("✅ SKU do livro atualizado: '{}' (SKU: {})", atualizado.getTitulo(), atualizado.getSku());
        
        return LivroDTO.fromModel(atualizado);
    }
    
    @Transactional
    public void deletar(long id) {
        // Check permission before deletion
        authService.checkPermission(Permission.MANAGE_BOOKS);
        
        livroRepository.findById(id)
            .ifPresentOrElse(
                livro -> {
                    livroRepository.delete(livro);
                    log.info("✅ Livro excluído com sucesso: '{}'", livro.getTitulo());
                },
                () -> {
                    throw new IllegalArgumentException("Livro não encontrado com ID: " + id);
                }
            );
    }
    
    public Long contarTodos() {
        return livroRepository.count();
    }
    
    private String gerarProximoSku(TipoLivro tipo) {
        // This is a utility method, no permission check needed
        String prefixo = tipo == TipoLivro.MINI ? "MI" : "CO";
        Long ultimoNumero = livroRepository.findUltimoNumeroBySku(prefixo);
        return String.format("%s%04d", prefixo, ultimoNumero + 1);
    }
}
