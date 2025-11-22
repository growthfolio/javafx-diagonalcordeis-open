package com.diagonal.cordeis.controller;

import com.diagonal.cordeis.dto.ColecaoDTO;
import com.diagonal.cordeis.dto.ColecaoRequestDTO;
import com.diagonal.cordeis.dto.LivroDTO;
import com.diagonal.cordeis.model.TipoLivro;
import com.diagonal.cordeis.service.ColecaoService;
import com.diagonal.cordeis.service.LivroService;
import com.diagonal.cordeis.view.CaixaDeDialogo;
import com.diagonal.cordeis.view.Notificacao;
import com.diagonal.cordeis.view.ModalUtils;
import com.diagonal.cordeis.view.FXMLViewLoader;
import com.diagonal.cordeis.view.FXMLViewLoader.FXMLView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ColecaoController {

    private static final Logger log = LoggerFactory.getLogger(ColecaoController.class);

    private final ColecaoService colecaoService;
    private final LivroService livroService;

    @Autowired
    private FXMLViewLoader fxmlViewLoader;

    private final ObservableList<ColecaoDTO> colecoes = FXCollections.observableArrayList();
    private final ObservableList<LivroDTO> livrosSelecionados = FXCollections.observableArrayList();

    private ColecaoDTO colecaoSelecionada;

    // FORM FIELDS
    @FXML private TextField skuField;
    @FXML private TextField nomeField;
    @FXML private ComboBox<String> comboTipoColecao;
    @FXML private ScrollPane scrollPreviewLivros;
    @FXML private VBox containerPreviewLivros;

    // BUTTONS
    @FXML private Button btnGerarSku;
    @FXML private Button btnAbrirSelecaoLivros;
    @FXML private Button btnSalvar;
    @FXML private Button btnLimpar;

    // TABLE
    @FXML private TableView<ColecaoDTO> tabelaColecoes;
    @FXML private TableColumn<ColecaoDTO, String> colSku;
    @FXML private TableColumn<ColecaoDTO, String> colNome;
    @FXML private TableColumn<ColecaoDTO, Number> colQuantidade;
    @FXML private TableColumn<ColecaoDTO, String> colLivros;

    // ACTION BUTTONS
    @FXML private Button btnEditarColecao;
    @FXML private Button btnExcluirColecao;

    // LABELS
    @FXML private Label labelTotalColecoes;
    @FXML private Label labelFormulario;
    
    // Campos de busca
    @FXML private TextField campoBuscaColecoes;
    @FXML private Button btnLimparBuscaColecoes;
    @FXML private Label labelResultadosColecoes;
    
    // Lista filtrada para busca
    private final ObservableList<ColecaoDTO> colecoesFiltradas = FXCollections.observableArrayList();

    private final ObservableList<LivroDTO> todosLivros = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        log.info("Inicializando tela de coleções");
        configurarComponentes();
        carregarDados();
        configurarEventos();
        debugCarregamento();
        log.info("Inicialização da tela de coleções concluída. Tabela configurada com {} itens", 
                 colecoesFiltradas.size());
    }        private void configurarComponentes() {
            if (tabelaColecoes != null) {
                tabelaColecoes.setItems(colecoesFiltradas);
                configurarColunas();
            }
            if (comboTipoColecao != null) {
                comboTipoColecao.setItems(FXCollections.observableArrayList("NORMAL", "MINI", "ESPECIAL"));
                comboTipoColecao.setValue("NORMAL");
            }
            livrosSelecionados.addListener((javafx.collections.ListChangeListener<LivroDTO>) c -> atualizarPreviewLivros());
            configurarBusca();
        }

    private void configurarColunas() {
        // Configurar valores das colunas
        colSku.setCellValueFactory(cell -> cell.getValue().skuProperty());
        colNome.setCellValueFactory(cell -> cell.getValue().nomeProperty());
        colQuantidade.setCellValueFactory(cell -> cell.getValue().quantidadeLivrosProperty());
        colLivros.setCellValueFactory(cell -> cell.getValue().nomesLivrosProperty());
        
        // Tornar tabela editável
        tabelaColecoes.setEditable(true);
        
        // Configurar edição inline para nome
        colNome.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        colNome.getStyleClass().add("editable-cell");
        colNome.setOnEditCommit(event -> {
            ColecaoDTO colecao = event.getRowValue();
            String novoNome = event.getNewValue();
            
            try {
                colecaoService.atualizarNome(colecao.getId(), novoNome);
                colecao.setNome(novoNome);
                
                Notificacao.sucesso("Nome da coleção atualizado: " + novoNome);
                log.info("Nome da coleção ID {} atualizado para: {}", colecao.getId(), novoNome);
                
            } catch (Exception e) {
                event.getTableView().refresh();
                Notificacao.erro("Erro ao atualizar nome: " + e.getMessage());
                log.error("Erro ao atualizar nome da coleção ID {}: {}", colecao.getId(), e.getMessage());
            }
        });
        
        // Configurar edição inline para SKU
        colSku.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        colSku.getStyleClass().add("editable-cell");
        colSku.setOnEditCommit(event -> {
            ColecaoDTO colecao = event.getRowValue();
            String novoSku = event.getNewValue();
            
            try {
                colecaoService.atualizarSku(colecao.getId(), novoSku);
                colecao.setSku(novoSku);
                
                Notificacao.sucesso("SKU da coleção atualizado: " + novoSku);
                log.info("SKU da coleção ID {} atualizado para: {}", colecao.getId(), novoSku);
                
            } catch (Exception e) {
                event.getTableView().refresh();
                Notificacao.erro("Erro ao atualizar SKU: " + e.getMessage());
                log.error("Erro ao atualizar SKU da coleção ID {}: {}", colecao.getId(), e.getMessage());
            }
        });
        
        configurarColunaLivros();
    }

    private void configurarColunaLivros() {
        colLivros.setCellFactory(column -> new TableCell<>() {
            private final Button btnVerLivros = new Button("📚 Ver livros");
            {
                btnVerLivros.setStyle("-fx-font-size: 10px; -fx-cursor: hand;");
                btnVerLivros.setOnAction(event -> {
                    ColecaoDTO colecao = getTableRow().getItem();
                    if (colecao != null) {
                        abrirModalLivrosDaColecao(colecao);
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(btnVerLivros);
                    setText(null);
                    // Centralizar o botão na célula
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
    }

private void abrirModalLivrosDaColecao(ColecaoDTO colecao) {
    try {
        var colecaoEntity = colecaoService.buscarColecaoComLivrosPorId(colecao.getId());
        List<LivroDTO> livrosDTO = new ArrayList<>();
        
        if (colecaoEntity.isPresent()) {
            for (var livro : colecaoEntity.get().getLivros()) {
                LivroDTO livroDTO = livroService.buscarPorId(livro.getId());
                if (livroDTO != null) {
                    livrosDTO.add(livroDTO);
                }
            }
        }

        // Criação da interface
        BorderPane root = new BorderPane();
        root.setPrefSize(650, 500);
        root.setStyle("-fx-background-color: white;");
        
        // Header com o título da coleção
        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: #9C27B0; -fx-padding: 15 20;");
        
        Label titulo = new Label("Livros da Coleção: " + colecao.getNome());
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label subtitulo = new Label("SKU: " + colecao.getSku() + " • " + livrosDTO.size() + (livrosDTO.size() == 1 ? " livro" : " livros"));
        subtitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: #E1BEE7;");
        
        header.getChildren().addAll(titulo, subtitulo);
        root.setTop(header);
        
        // Tabela com os livros
        TableView<LivroDTO> tabelaLivros = new TableView<>();
        tabelaLivros.setItems(FXCollections.observableArrayList(livrosDTO));
        tabelaLivros.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1;");
        tabelaLivros.setEditable(true);
        
        // Colunas da tabela
        TableColumn<LivroDTO, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(cell -> cell.getValue().tituloProperty());
        colTitulo.setPrefWidth(250);
        
        // Configurar edição inline para título
        colTitulo.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        colTitulo.setOnEditCommit(event -> {
            LivroDTO livro = event.getRowValue();
            String novoTitulo = event.getNewValue();
            
            try {
                livroService.atualizarTitulo(livro.getId(), novoTitulo);
                livro.setTitulo(novoTitulo);
                log.info("Título atualizado no modal da coleção: {} -> {}", livro.getSku(), novoTitulo);
                
            } catch (Exception e) {
                event.getTableView().refresh();
                log.error("Erro ao atualizar título no modal da coleção: {}", e.getMessage());
            }
        });
        
        TableColumn<LivroDTO, String> colSku = new TableColumn<>("SKU");
        colSku.setCellValueFactory(cell -> cell.getValue().skuProperty());
        colSku.setPrefWidth(100);
        
        TableColumn<LivroDTO, TipoLivro> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(cell -> cell.getValue().tipoProperty());
        colTipo.setPrefWidth(80);
        
        TableColumn<LivroDTO, Number> colPaginas = new TableColumn<>("Páginas");
        colPaginas.setCellValueFactory(cell -> cell.getValue().paginasProperty());
        colPaginas.setPrefWidth(80);
        
        // Coluna com botão para visualizar detalhes
        TableColumn<LivroDTO, Void> colAcoes = new TableColumn<>("Ações");
        colAcoes.setPrefWidth(100);
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnVisualizar = new Button("👁️ Detalhes");
            {
                btnVisualizar.setStyle("-fx-font-size: 11px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                btnVisualizar.setOnAction(event -> {
                    LivroDTO livro = getTableRow().getItem();
                    if (livro != null) {
                        // Aqui poderia abrir o modal de detalhes do livro
                        Notificacao.informacao("Detalhes do livro: " + livro.getTitulo());
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(btnVisualizar);
                }
            }
        });
        
        // Adicionar colunas à tabela
        tabelaLivros.getColumns().add(colTitulo);
        tabelaLivros.getColumns().add(colSku);
        tabelaLivros.getColumns().add(colTipo);
        tabelaLivros.getColumns().add(colPaginas);
        tabelaLivros.getColumns().add(colAcoes);
        
        // Mensagem quando não há livros
        tabelaLivros.setPlaceholder(new Label("Nenhum livro nesta coleção"));
        
        // Adicionar tabela ao centro
        VBox centroContainer = new VBox(10);
        centroContainer.setStyle("-fx-padding: 15;");
        
        HBox filtroBox = new HBox(10);
        filtroBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label labelInfo = new Label("📚 Total de livros nesta coleção: " + livrosDTO.size());
        labelInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        filtroBox.getChildren().add(labelInfo);
        
        centroContainer.getChildren().addAll(filtroBox, tabelaLivros);
        root.setCenter(centroContainer);
        
        // Footer com botões
        HBox footer = new HBox(10);
        footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        footer.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5; -fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");
        
        Button btnEditar = new Button("✏️ Editar Coleção");
        btnEditar.setStyle("-fx-background-color: #FFA000; -fx-text-fill: white;");
        btnEditar.setOnAction(event -> {
            // Fechar o modal e abrir edição
            Stage stage = (Stage) btnEditar.getScene().getWindow();
            stage.close();
            
            // Preencher o formulário para edição
            preencherFormulario(colecao);
            if (btnSalvar != null) btnSalvar.setText("💾 Atualizar Coleção");
            if (labelFormulario != null) labelFormulario.setText("Editar Coleção");
        });
        
        Button btnFechar = new Button("✖ Fechar");
        btnFechar.setStyle("-fx-background-color: #757575; -fx-text-fill: white;");
        btnFechar.setOnAction(event -> {
            Stage stage = (Stage) btnFechar.getScene().getWindow();
            stage.close();
        });
        
        footer.getChildren().addAll(btnEditar, btnFechar);
        root.setBottom(footer);
        
        // Configuração do modal com dimensionamento automático
        Stage modal = ModalUtils.createModalStage("Livros da Coleção: " + colecao.getNome(), root, null);
        modal.showAndWait();

    } catch (Exception e) {
        log.error("Erro ao exibir modal de livros da coleção", e);
        Notificacao.erro("Não foi possível exibir os livros da coleção.");
    }
}

    private void configurarEventos() {
            if (tabelaColecoes != null) {
                tabelaColecoes.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        colecaoSelecionada = newSelection;
                        if (newSelection != null) {
                            preencherFormulario(newSelection);
                        }
                    });
                tabelaColecoes.setRowFactory(tv -> {
                    TableRow<ColecaoDTO> row = new TableRow<>();
                    row.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && !row.isEmpty()) {
                            editarColecao();
                        }
                    });
                    return row;
                });
            }
        }

        private void carregarDados() {
            carregarColecoes();
            carregarLivrosDisponiveis();
            atualizarContadores();
        }

        private void carregarColecoes() {
            log.info("Carregando coleções...");
            List<ColecaoDTO> colecoesDoBanco = colecaoService.listarTodas();
            log.info("Encontradas {} coleções no banco de dados", colecoesDoBanco.size());
            
            colecoes.setAll(colecoesDoBanco);
            
            // Sincronizar com a lista filtrada (inicialmente mostra todas)
            colecoesFiltradas.setAll(colecoes);
            
            log.info("Carregadas {} coleções na lista principal e {} na lista filtrada", 
                     colecoes.size(), colecoesFiltradas.size());
        }

        private void carregarLivrosDisponiveis() {
            log.info("Carregando livros disponíveis...");
            todosLivros.setAll(livroService.listarTodos());
        }

        @FXML
        public void abrirModalSelecaoLivros() {
            if (nomeField.getText() == null || nomeField.getText().trim().isEmpty()) {
                Notificacao.informacao("Digite o nome da coleção antes de selecionar os livros.");
                nomeField.requestFocus();
                return;
            }
            try {
                FXMLViewLoader.FXMLView<ModalSelecaoLivrosController> view = fxmlViewLoader.loadWithController("/fxml/modal-selecao-livros.fxml");
                Parent root = view.root();
                ModalSelecaoLivrosController modalController = view.controller();
                modalController.setNomeColecao(nomeField.getText());
                modalController.setLivrosJaSelecionados(new ArrayList<>(livrosSelecionados));
                modalController.setOnConfirmar(livros -> {
                    livrosSelecionados.setAll(livros);
                    atualizarPreviewLivros();
                    Notificacao.sucesso(livros.size() + " livro(s) selecionado(s) para a coleção!");
                });
                modalController.setOnCancelar(() -> log.info("Seleção de livros cancelada pelo usuário"));
                Stage modalStage = ModalUtils.createModalStage("Selecionar Livros - " + nomeField.getText(), root, null);
                modalController.setModalStage(modalStage);
                modalStage.showAndWait();
            } catch (Exception e) {
                log.error("Erro ao abrir modal de seleção de livros", e);
                Notificacao.erro("Erro ao abrir seleção de livros: " + e.getMessage());
            }
        }

        private void atualizarPreviewLivros() {
            if (containerPreviewLivros == null) return;
            containerPreviewLivros.getChildren().clear();
            if (livrosSelecionados.isEmpty()) {
                Label placeholder = new Label("👆 Clique em 'Selecionar Livros' para escolher os livros desta coleção");
                placeholder.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-font-style: italic; -fx-text-alignment: center;");
                placeholder.setMaxWidth(Double.MAX_VALUE);
                containerPreviewLivros.getChildren().add(placeholder);
            } else {
                for (LivroDTO livro : livrosSelecionados) {
                    Label labelLivro = new Label(String.format("📖 %s (%s) - %s",
                        livro.getTitulo(), livro.getSku(), livro.getTipo()));
                    labelLivro.setStyle("-fx-font-size: 11px; -fx-text-fill: #333; -fx-padding: 2 0;");
                    containerPreviewLivros.getChildren().add(labelLivro);
                }
            }
            atualizarContadores();
        }

        @FXML
        public void gerarSku() {
            String novoSku = colecaoService.gerarProximoSku();
            skuField.setText(novoSku);
            log.info("SKU gerado: {}", novoSku);
        }

        @FXML
        public void salvarColecao() {
            log.info("Ação: salvarColecao()");
            if (nomeField.getText() == null || nomeField.getText().trim().isEmpty()) {
                Notificacao.erro("O nome da coleção é obrigatório.");
                nomeField.requestFocus();
                return;
            }
            if (skuField.getText() == null || skuField.getText().trim().isEmpty()) {
                Notificacao.erro("O SKU da coleção é obrigatório.");
                skuField.requestFocus();
                return;
            }
            if (livrosSelecionados.isEmpty()) {
                Notificacao.erro("Selecione pelo menos um livro para a coleção.");
                return;
            }
            try {
                List<Long> idsLivros = livrosSelecionados.stream()
                        .map(LivroDTO::getId)
                        .toList();
                ColecaoRequestDTO request = new ColecaoRequestDTO(
                        nomeField.getText().trim(),
                        skuField.getText().trim(),
                        idsLivros
                );
                if (colecaoSelecionada == null) {
                    var response = colecaoService.criarColecao(request);
                    Notificacao.sucesso("Coleção '" + response.getNome() + "' criada com sucesso!\nSKU: " + response.getSku());
                } else {
                    var response = colecaoService.atualizarColecao(colecaoSelecionada.getId(), request);
                    Notificacao.sucesso("Coleção '" + response.getNome() + "' atualizada com sucesso!");
                }
                limparFormulario();
                carregarColecoes();
                atualizarContadores();
            } catch (Exception e) {
                log.error("Erro ao salvar coleção", e);
                Notificacao.erro("Erro ao salvar coleção: " + e.getMessage());
            }
        }

        @FXML
        public void limparFormulario() {
            if (skuField != null) skuField.clear();
            if (nomeField != null) nomeField.clear();
            livrosSelecionados.clear();
            if (tabelaColecoes != null) tabelaColecoes.getSelectionModel().clearSelection();
            colecaoSelecionada = null;
            if (btnSalvar != null) btnSalvar.setText("💾 Salvar Coleção");
            if (labelFormulario != null) labelFormulario.setText("Nova Coleção");
            log.info("Formulário limpo");
        }

        @FXML
        public void editarColecao() {
            if (tabelaColecoes == null) return;
            ColecaoDTO selecionada = tabelaColecoes.getSelectionModel().getSelectedItem();
            if (selecionada == null) {
                Notificacao.informacao("Selecione uma coleção para editar.");
                return;
            }
            preencherFormulario(selecionada);
            if (btnSalvar != null) btnSalvar.setText("💾 Atualizar Coleção");
            if (labelFormulario != null) labelFormulario.setText("Editar Coleção");
            log.info("Editando coleção: {}", selecionada.getNome());
        }

        @FXML
        public void excluirColecao() {
            if (tabelaColecoes == null) return;
            ColecaoDTO selecionada = tabelaColecoes.getSelectionModel().getSelectedItem();
            if (selecionada == null) {
                Notificacao.informacao("Selecione uma coleção para excluir.");
                return;
            }
            if (CaixaDeDialogo.confirmar("Confirmar Exclusão",
                    "Deseja realmente excluir a coleção '" + selecionada.getNome() + "'?\n\n" +
                    "Esta ação não poderá ser desfeita.")) {
                try {
                    colecaoService.excluirColecao(selecionada.getId());
                    Notificacao.sucesso("Coleção '" + selecionada.getNome() + "' excluída com sucesso!");
                    limparFormulario();
                    carregarColecoes();
                    atualizarContadores();
                } catch (Exception e) {
                    log.error("Erro ao excluir coleção", e);
                    Notificacao.erro("Erro ao excluir coleção: " + e.getMessage());
                }
            }
        }

        private void preencherFormulario(ColecaoDTO dto) {
            if (dto == null) return;
            if (skuField != null) skuField.setText(dto.getSku());
            if (nomeField != null) nomeField.setText(dto.getNome());
            livrosSelecionados.clear();
            try {
                var colecaoCompleta = colecaoService.buscarColecaoComLivrosPorId(dto.getId());
                if (colecaoCompleta.isPresent()) {
                    var livrosDaColecao = colecaoCompleta.get().getLivros().stream()
                            .map(livro -> livroService.buscarPorId(livro.getId()))
                            .filter(livroDto -> livroDto != null)
                            .toList();
                    livrosSelecionados.addAll(livrosDaColecao);
                }
            } catch (Exception e) {
                log.error("Erro ao carregar livros da coleção", e);
            }
            colecaoSelecionada = dto;
            log.info("Formulário preenchido com dados da coleção: {}", dto.getNome());
        }

        private void atualizarContadores() {
            int total = colecoes.size();
            int resultados = colecoesFiltradas.size();
            
            if (labelTotalColecoes != null) {
                labelTotalColecoes.setText(total + (total == 1 ? " coleção" : " coleções"));
            }
            
            if (labelResultadosColecoes != null) {
                labelResultadosColecoes.setText(resultados + (resultados == 1 ? " resultado" : " resultados"));
            }
        }
    
        private void configurarBusca() {
        if (campoBuscaColecoes != null) {
            // Configurar listener para busca em tempo real
            campoBuscaColecoes.textProperty().addListener((obs, oldVal, newVal) -> {
                filtrarColecoes(newVal);
            });
            
            // Configurar botão de limpar
            if (btnLimparBuscaColecoes != null) {
                btnLimparBuscaColecoes.setOnAction(e -> limparBuscaColecoes());
                btnLimparBuscaColecoes.setVisible(false); // Ocultar inicialmente
            }
        }
    }
    
    private void filtrarColecoes(String textoBusca) {
        if (textoBusca == null || textoBusca.trim().isEmpty()) {
            // Se busca está vazia, mostrar todas as coleções
            colecoesFiltradas.setAll(colecoes);
            if (btnLimparBuscaColecoes != null) {
                btnLimparBuscaColecoes.setVisible(false);
            }
        } else {
            // Filtrar coleções baseado no texto de busca
            String busca = textoBusca.toLowerCase().trim();
            List<ColecaoDTO> resultados = colecoes.stream()
                .filter(colecao -> 
                    colecao.getNome().toLowerCase().contains(busca) ||
                    colecao.getSku().toLowerCase().contains(busca)
                )
                .collect(Collectors.toList());
            
            colecoesFiltradas.setAll(resultados);
            
            if (btnLimparBuscaColecoes != null) {
                btnLimparBuscaColecoes.setVisible(true);
            }
        }
        
        atualizarContadores();
    }
    
    @FXML
    public void limparBuscaColecoes() {
        if (campoBuscaColecoes != null) {
            campoBuscaColecoes.clear();
        }
    }

    private void debugCarregamento() {
        log.info("=== DEBUG CARREGAMENTO ===");
        log.info("colecoes.size(): {}", colecoes.size());
        log.info("colecoesFiltradas.size(): {}", colecoesFiltradas.size());
        log.info("tabelaColecoes != null: {}", tabelaColecoes != null);
        
        if (tabelaColecoes != null) {
            log.info("tabelaColecoes.getItems().size(): {}", tabelaColecoes.getItems().size());
            log.info("tabelaColecoes.getColumns().size(): {}", tabelaColecoes.getColumns().size());
        }
        
        log.info("Primeira coleção (se existir):");
        if (!colecoes.isEmpty()) {
            ColecaoDTO primeira = colecoes.get(0);
            log.info("  ID: {}", primeira.getId());
            log.info("  Nome: {}", primeira.getNome());
            log.info("  SKU: {}", primeira.getSku());
            log.info("  Quantidade: {}", primeira.getQuantidadeLivros());
        }
        log.info("=== FIM DEBUG ===");
    }
}