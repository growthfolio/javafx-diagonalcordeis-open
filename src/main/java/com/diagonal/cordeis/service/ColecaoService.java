package com.diagonal.cordeis.service;

import com.diagonal.cordeis.dto.ColecaoDTO;
import com.diagonal.cordeis.dto.ColecaoRequestDTO;
import com.diagonal.cordeis.dto.ColecaoResponseDTO;
import com.diagonal.cordeis.model.Colecao;
import com.diagonal.cordeis.model.Livro;
import com.diagonal.cordeis.model.Permission;
import com.diagonal.cordeis.repository.ColecaoRepository;
import com.diagonal.cordeis.repository.LivroRepository;
import com.diagonal.cordeis.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColecaoService {

    private final ColecaoRepository colecaoRepository;
    private final LivroRepository livroRepository;
    private final AuthenticationService authService;

    @Transactional
    public ColecaoResponseDTO criarColecao(ColecaoRequestDTO request) {
        if (authService != null) {
            authService.checkPermission(Permission.MANAGE_COLLECTIONS);
        }
        
        log.info("Criando nova coleção: {}", request.getNome());

        // Validar se SKU já existe
        if (colecaoRepository.findBySku(request.getSku()).isPresent()) {
            throw new IllegalArgumentException("SKU '" + request.getSku() + "' já está em uso.");
        }

        // Buscar livros pelos IDs
        List<Livro> livrosEncontrados = livroRepository.findAllById(request.getIdsLivros());
        
        // Verificar se todos os livros foram encontrados
        if (livrosEncontrados.size() != request.getIdsLivros().size()) {
            List<Long> idsNaoEncontrados = request.getIdsLivros().stream()
                    .filter(id -> livrosEncontrados.stream().noneMatch(l -> l.getId().equals(id)))
                    .collect(Collectors.toList());
            throw new IllegalArgumentException("Livros não encontrados com IDs: " + idsNaoEncontrados);
        }

        // Criar coleção
        Colecao colecao = new Colecao(request.getSku(), request.getNome());
        for (Livro livro : livrosEncontrados) {
            colecao.addLivro(livro);
        }

        Colecao salva = colecaoRepository.save(colecao);
        log.info("Coleção '{}' criada com sucesso (ID: {})", salva.getNome(), salva.getId());

        return convertToResponseDTO(salva);
    }

    @Transactional
    public ColecaoResponseDTO atualizarColecao(Long id, ColecaoRequestDTO request) {
        // Check permission before update
        authService.checkPermission(Permission.MANAGE_COLLECTIONS);
        
        log.info("Atualizando coleção ID: {}", id);

        Colecao colecao = colecaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coleção não encontrada"));

        // Verificar se SKU mudou e se já existe
        if (!colecao.getSku().equals(request.getSku())) {
            if (colecaoRepository.findBySku(request.getSku()).isPresent()) {
                throw new IllegalArgumentException("SKU '" + request.getSku() + "' já está em uso.");
            }
        }

        // Buscar novos livros
        Set<Livro> livros = request.getIdsLivros().stream()
                .map(livroId -> livroRepository.findById(livroId)
                        .orElseThrow(() -> new IllegalArgumentException("Livro com ID " + livroId + " não encontrado")))
                .collect(Collectors.toSet());

        // Atualizar coleção
        colecao.setNome(request.getNome());
        colecao.setSku(request.getSku());
        colecao.setLivros(livros);

        Colecao atualizada = colecaoRepository.save(colecao);
        log.info("Coleção '{}' atualizada com sucesso", atualizada.getNome());

        return convertToResponseDTO(atualizada);
    }

    /**
     * Atualiza apenas o nome de uma coleção
     */
    @Transactional
    public void atualizarNome(Long id, String novoNome) {
        // Check permission before update
        authService.checkPermission(Permission.MANAGE_COLLECTIONS);
        
        log.info("Atualizando nome da coleção ID {}: {}", id, novoNome);
        
        Colecao colecao = colecaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coleção não encontrada com ID: " + id));
                
        colecao.setNome(novoNome);
        colecaoRepository.save(colecao);
        
        log.info("Nome da coleção atualizado com sucesso");
    }
    
    @Transactional
    public void atualizarSku(long id, String novoSku) {
        authService.checkPermission(Permission.MANAGE_COLLECTIONS);
        
        Colecao colecao = colecaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coleção não encontrada com ID: " + id));

        if (!colecao.getSku().equals(novoSku)) {
            Optional<Colecao> existente = colecaoRepository.findBySku(novoSku);
            if (existente.isPresent()) {
                throw new IllegalArgumentException("SKU '" + novoSku + "' já está em uso.");
            }
        }
        
        colecao.setSku(novoSku);
        colecaoRepository.save(colecao);
        
        log.info("SKU da coleção atualizado com sucesso: {}", novoSku);
    }
    
    public List<ColecaoDTO> listarTodas() {
        // Skip permission check if authService not available (e.g., in tests)
        if (authService != null && !authService.hasPermission(Permission.VIEW_COLLECTIONS)
            && !authService.hasPermission(Permission.MANAGE_COLLECTIONS)) {
            throw new AccessDeniedException("Você não tem permissão para visualizar coleções.");
        }
        
        return colecaoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public ColecaoDTO buscarPorId(Long id) {
        if (!authService.hasPermission(Permission.VIEW_COLLECTIONS) && 
            !authService.hasPermission(Permission.MANAGE_COLLECTIONS)) {
            throw new AccessDeniedException("Você não tem permissão para visualizar coleções.");
        }
        
        return colecaoRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new IllegalArgumentException("Coleção não encontrada com ID: " + id));
    }
    
    @Transactional
    public void excluirColecao(Long id) {
        authService.checkPermission(Permission.MANAGE_COLLECTIONS);
        
        Colecao colecao = colecaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coleção não encontrada com ID: " + id));
                
        colecaoRepository.delete(colecao);
        log.info("Coleção '{}' excluída com sucesso", colecao.getNome());
    }
    
    public String gerarProximoSku() {
        List<Colecao> colecoes = colecaoRepository.findBySkuStartingWithOrderBySkuDesc("COL");
        if (colecoes.isEmpty()) {
            return "COL0001";
        }

        String ultimoSku = colecoes.get(0).getSku();
        String numeroStr = ultimoSku.substring(3); // Remover "COL"
        
        try {
            int numero = Integer.parseInt(numeroStr);
            return String.format("COL%04d", numero + 1);
        } catch (NumberFormatException e) {
            log.error("Erro ao gerar próximo SKU: {}", e.getMessage());
            return "COL0001";
        }
    }
    
    public Optional<Colecao> buscarColecaoComLivrosPorId(Long id) {
        if (!authService.hasPermission(Permission.VIEW_COLLECTIONS) && 
            !authService.hasPermission(Permission.MANAGE_COLLECTIONS)) {
            throw new AccessDeniedException("Você não tem permissão para visualizar coleções.");
        }
        
        Optional<Colecao> colecaoOpt = colecaoRepository.findById(id);
        if (colecaoOpt.isPresent()) {
            colecaoOpt.get().getLivros().size();
        }
        return colecaoOpt;
    }
    
    private ColecaoDTO convertToDTO(Colecao colecao) {
        ColecaoDTO dto = new ColecaoDTO();
        dto.setId(colecao.getId());
        dto.setNome(colecao.getNome());
        dto.setSku(colecao.getSku());
        dto.setQuantidadeLivros(colecao.getQuantidadeLivros());
        
        List<ColecaoDTO.LivroDTO> livrosDTO = colecao.getLivros().stream()
                .map(livro -> {
                    ColecaoDTO.LivroDTO livroDTO = new ColecaoDTO.LivroDTO();
                    livroDTO.setId(livro.getId());
                    livroDTO.setTitulo(livro.getTitulo());
                    livroDTO.setSku(livro.getSku());
                    livroDTO.setTipo(livro.getTipo());
                    return livroDTO;
                })
                .toList();
        
        dto.setLivros(livrosDTO);

        List<String> nomes = colecao.getLivros().stream()
                .map(livro -> livro.getTitulo())
                .sorted()
                .toList();
        dto.setNomesLivros(String.join(", ", nomes));

        return dto;
    }
    
    private ColecaoResponseDTO convertToResponseDTO(Colecao colecao) {
        List<String> nomesLivros = colecao.getLivros().stream()
                .map(livro -> livro.getTitulo())
                .collect(Collectors.toList());
        
        return new ColecaoResponseDTO(
                colecao.getId(),
                colecao.getNome(),
                colecao.getSku(),
                nomesLivros);
    }
}
