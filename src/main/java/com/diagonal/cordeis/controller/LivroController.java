package com.diagonal.cordeis.controller;

import com.diagonal.cordeis.dto.LivroDTO;
import com.diagonal.cordeis.model.TipoLivro;
import com.diagonal.cordeis.service.LivroService;
import com.diagonal.cordeis.view.CaixaDeDialogo;
import com.diagonal.cordeis.view.Notificacao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LivroController {

    private static final Logger log = LoggerFactory.getLogger(LivroController.class);

    private final LivroService livroService;

    private final ObservableList<LivroDTO> livros = FXCollections.observableArrayList();

    private LivroDTO livroSelecionado;
    private File arquivoSelecionado;

    @FXML
    private TextField tituloField;
    @FXML
    private TextField skuField;
    @FXML
    private TextField caminhoPdfField;
    @FXML
    private ComboBox<TipoLivro> comboTipo;
    @FXML
    private TableView<LivroDTO> tabelaLivros;
    @FXML
    private TableColumn<LivroDTO, String> colSku;
    @FXML
    private TableColumn<LivroDTO, String> colTitulo;
    @FXML
    private TableColumn<LivroDTO, Number> colPaginas;
    @FXML
    private TableColumn<LivroDTO, String> colArquivo;
    @FXML
    private TableColumn<LivroDTO, String> colTipo;
    @FXML
    private Label labelTotalLivros;
    
    // Campos de busca
    @FXML private TextField campoBuscaLivros;
    @FXML private Button btnLimparBuscaLivros;
    @FXML private Label labelResultadosLivros;
    
    // Lista filtrada para busca
    private final ObservableList<LivroDTO> livrosFiltrados = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        log.info("Inicializando tela de livros");

        comboTipo.setItems(FXCollections.observableArrayList(TipoLivro.values()));
        comboTipo.setValue(TipoLivro.NORMAL);
        
        // Configurar tabela com lista filtrada
        tabelaLivros.setItems(livrosFiltrados);

        configurarColunas();
        configurarBusca();
        carregarLivros();

        tabelaLivros.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> preencherFormulario(newSel));
    }

    private void configurarColunas() {
        // Configurar valores das colunas
        colSku.setCellValueFactory(cell -> cell.getValue().skuProperty());
        colTitulo.setCellValueFactory(cell -> cell.getValue().tituloProperty());
        colPaginas.setCellValueFactory(cell -> cell.getValue().paginasProperty());
        colArquivo.setCellValueFactory(cell -> cell.getValue().nomeArquivoProperty());
        colTipo.setCellValueFactory(cell -> cell.getValue().tipoProperty().asString());
        
        // Tornar tabela editável
        tabelaLivros.setEditable(true);
        
        // Configurar edição inline para título
        colTitulo.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        colTitulo.setOnEditCommit(event -> {
            LivroDTO livro = event.getRowValue();
            String novoTitulo = event.getNewValue();
            
            try {
                livroService.atualizarTitulo(livro.getId(), novoTitulo);
                livro.setTitulo(novoTitulo);
                
                Notificacao.sucesso("Título atualizado: " + novoTitulo);
                log.info("Título do livro ID {} atualizado para: {}", livro.getId(), novoTitulo);
                
            } catch (Exception e) {
                event.getTableView().refresh();
                Notificacao.erro("Erro ao atualizar título: " + e.getMessage());
                log.error("Erro ao atualizar título do livro ID {}: {}", livro.getId(), e.getMessage());
            }
        });
        
        // Configurar edição inline para SKU
        colSku.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        colSku.setOnEditCommit(event -> {
            LivroDTO livro = event.getRowValue();
            String novoSku = event.getNewValue();
            
            try {
                livroService.atualizarSku(livro.getId(), novoSku);
                livro.setSku(novoSku);
                
                Notificacao.sucesso("SKU atualizado: " + novoSku);
                log.info("SKU do livro ID {} atualizado para: {}", livro.getId(), novoSku);
                
            } catch (Exception e) {
                event.getTableView().refresh();
                Notificacao.erro("Erro ao atualizar SKU: " + e.getMessage());
                log.error("Erro ao atualizar SKU do livro ID {}: {}", livro.getId(), e.getMessage());
            }
        });
    }

    private void carregarLivros() {
        log.info("Carregando livros...");
        livros.setAll(livroService.listarTodos());
        
        // Se não há busca ativa, mostrar todos os livros
        if (campoBuscaLivros == null || campoBuscaLivros.getText().trim().isEmpty()) {
            livrosFiltrados.setAll(livros);
        } else {
            // Reaplicar filtro se há busca ativa
            filtrarLivros(campoBuscaLivros.getText());
        }
        
        atualizarContadorLivros();
        atualizarContadorResultados();
    }

    private void atualizarContadorLivros() {
        int total = livros.size();
        if (labelTotalLivros != null) {
            labelTotalLivros.setText(total + (total == 1 ? " livro" : " livros"));
        }
    }

    @FXML
    public void salvarLivro() {
        log.info("Ação: salvarLivro()");

        // Validações básicas
        if (tituloField.getText() == null || tituloField.getText().trim().isEmpty()) {
            Notificacao.erro("O título do livro é obrigatório.");
            tituloField.requestFocus();
            return;
        }

        if (arquivoSelecionado == null) {
            Notificacao.erro("Selecione um arquivo PDF para o livro.");
            return;
        }

        try {
            if (livroSelecionado == null) {
                // Novo livro
                LivroDTO dto = new LivroDTO();
                dto.setTitulo(tituloField.getText().trim());
                dto.setCaminhoPdf(arquivoSelecionado.getAbsolutePath());
                // Tipo e SKU serão definidos automaticamente pelo service

                LivroDTO salvo = livroService.salvar(dto);
                livros.add(salvo);
                Notificacao.sucesso("Livro '" + salvo.getTitulo() + "' salvo com sucesso!\nSKU: " + salvo.getSku());
            } else {
                // Atualizar existente
                livroSelecionado.setTitulo(tituloField.getText().trim());
                if (arquivoSelecionado != null) {
                    livroSelecionado.setCaminhoPdf(arquivoSelecionado.getAbsolutePath());
                }

                LivroDTO atualizado = livroService.atualizar(livroSelecionado);
                int index = livros.indexOf(livroSelecionado);
                livros.set(index, atualizado);
                Notificacao.sucesso("Livro '" + atualizado.getTitulo() + "' atualizado com sucesso!\nSKU: " + atualizado.getSku());
            }

            limparFormulario();
            atualizarContadorLivros();
        } catch (Exception e) {
            log.error("Erro ao salvar/atualizar livro", e);
            Notificacao.erro("Erro ao salvar livro: " + e.getMessage());
        }
    }

    @FXML
    public void limparFormulario() {
        comboTipo.setValue(TipoLivro.NORMAL);
        tituloField.clear();
        skuField.clear();
        caminhoPdfField.clear();
        tabelaLivros.getSelectionModel().clearSelection();
        livroSelecionado = null;
        arquivoSelecionado = null;
    }

    @FXML
    public void excluirLivro() {
        LivroDTO selecionado = tabelaLivros.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            Notificacao.informacao("Selecione um livro para excluir.");
            return;
        }

        if (CaixaDeDialogo.confirmar("Confirmar Exclusão",
                "Deseja realmente excluir o livro '" + selecionado.getTitulo() + "'?")) {
            try {
                livroService.deletar(selecionado.getId());
                livros.remove(selecionado);
                limparFormulario();
                atualizarContadorLivros();
                Notificacao.sucesso("Livro '" + selecionado.getTitulo() + "' excluído com sucesso.");
            } catch (Exception e) {
                log.error("Erro ao excluir livro", e);
                Notificacao.erro("Erro ao excluir livro: " + e.getMessage());
            }
        }
    }


    @FXML
    public void escolherArquivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar PDF do Livro");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf")
        );

        File arquivo = fileChooser.showOpenDialog(null);
        if (arquivo != null) {
            // Validar se é um PDF válido
            try {
                if (com.diagonal.cordeis.util.ArquivoUtils.validarPdf(arquivo.getAbsolutePath())) {
                    arquivoSelecionado = arquivo;
                    caminhoPdfField.setText(arquivo.getName());

                    // Extrair e pré-visualizar informações automaticamente
                    String nomeArquivo = arquivo.getName();
                    
                    // Pré-visualizar SKU
                    String skuPrevisao = com.diagonal.cordeis.util.LivroUtils.extrairSku(nomeArquivo);
                    if (skuPrevisao != null) {
                        skuField.setText(skuPrevisao + " (prévia)");
                    } else {
                        skuField.setText("Será gerado automaticamente");
                    }
                    
                    // Pré-visualizar tipo
                    com.diagonal.cordeis.model.TipoLivro tipoPrevisao = com.diagonal.cordeis.util.LivroUtils.determinarTipoPorNome(nomeArquivo);
                    comboTipo.setValue(tipoPrevisao);

                    // Se o título estiver vazio, sugerir o título extraído
                    if (tituloField.getText() == null || tituloField.getText().trim().isEmpty()) {
                        String tituloExtraido = com.diagonal.cordeis.util.LivroUtils.extrairTitulo(nomeArquivo);
                        if (!tituloExtraido.isEmpty()) {
                            tituloField.setText(tituloExtraido);
                        }
                    }
                } else {
                    Notificacao.erro("O arquivo selecionado não é um PDF válido.");
                }
            } catch (Exception e) {
                log.error("Erro ao validar PDF", e);
                Notificacao.erro("Erro ao processar o arquivo: " + e.getMessage());
            }
        }
    }

    private void preencherFormulario(LivroDTO dto) {
        if (dto == null) return;

        comboTipo.setValue(dto.getTipo());
        tituloField.setText(dto.getTitulo());
        arquivoSelecionado = new File(dto.getCaminhoPdf());
        caminhoPdfField.setText(arquivoSelecionado.getName());
        livroSelecionado = dto;
    }
    
    private void configurarBusca() {
        if (campoBuscaLivros != null) {
            // Configurar listener para busca em tempo real
            campoBuscaLivros.textProperty().addListener((obs, oldVal, newVal) -> {
                filtrarLivros(newVal);
            });
            
            // Configurar botão de limpar
            if (btnLimparBuscaLivros != null) {
                btnLimparBuscaLivros.setOnAction(e -> limparBuscaLivros());
                btnLimparBuscaLivros.setVisible(false); // Ocultar inicialmente
            }
        }
    }
    
    private void filtrarLivros(String textoBusca) {
        if (textoBusca == null || textoBusca.trim().isEmpty()) {
            // Se busca está vazia, mostrar todos os livros
            livrosFiltrados.setAll(livros);
            if (btnLimparBuscaLivros != null) {
                btnLimparBuscaLivros.setVisible(false);
            }
        } else {
            // Filtrar livros baseado no texto de busca
            String busca = textoBusca.toLowerCase().trim();
            List<LivroDTO> resultados = livros.stream()
                .filter(livro -> 
                    livro.getTitulo().toLowerCase().contains(busca) ||
                    livro.getSku().toLowerCase().contains(busca) ||
                    livro.getTipo().toString().toLowerCase().contains(busca) ||
                    livro.getNomeArquivo().toLowerCase().contains(busca)
                )
                .toList();
            
            livrosFiltrados.setAll(resultados);
            
            if (btnLimparBuscaLivros != null) {
                btnLimparBuscaLivros.setVisible(true);
            }
        }
        
        // Atualizar contador de resultados
        atualizarContadorResultados();
    }
    
    @FXML
    public void limparBuscaLivros() {
        if (campoBuscaLivros != null) {
            campoBuscaLivros.clear();
        }
    }
    
    private void atualizarContadorResultados() {
        int resultados = livrosFiltrados.size();
        int total = livros.size();
        
        if (labelResultadosLivros != null) {
            if (campoBuscaLivros != null && !campoBuscaLivros.getText().trim().isEmpty()) {
                labelResultadosLivros.setText(resultados + " de " + total + " livros");
            } else {
                labelResultadosLivros.setText(total + " livros");
            }
        }
    }
}
