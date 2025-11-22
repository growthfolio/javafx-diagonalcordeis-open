package com.diagonal.cordeis.controller;

import com.diagonal.cordeis.dto.ColecaoResponseDTO;
import com.diagonal.cordeis.dto.LivroDTO;
import com.diagonal.cordeis.dto.ColecaoDTO;
import com.diagonal.cordeis.dto.LivroImpressaoDTO;
import com.diagonal.cordeis.dto.ColecaoImpressaoDTO;
import com.diagonal.cordeis.model.Livro;
import com.diagonal.cordeis.model.Colecao;
import com.diagonal.cordeis.model.Permission;
import com.diagonal.cordeis.security.AuthenticationService;
import com.diagonal.cordeis.service.ImpressaoService;
import com.diagonal.cordeis.service.LivroService;
import com.diagonal.cordeis.service.ColecaoService;
import com.diagonal.cordeis.util.ImpressoraUtil;
import com.diagonal.cordeis.view.Notificacao;
import com.diagonal.cordeis.view.ModalUtils;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@Component
public class ImpressaoController {

    // Componentes da aba de livros
    @FXML private TabPane tabPane;
    @FXML private Tab tabLivro;
    @FXML private Tab tabColecao;

    // Componentes de busca e filtros para livros
    @FXML private TextField campoBuscaLivros;
    @FXML private Button btnLimparBuscaLivros;

    // Componentes de seleção para livros
    @FXML private Button btnSelecionarTodosLivros;
    @FXML private Button btnLimparSelecaoLivros;
    @FXML private Label labelContadorLivros;

    // Componentes de impressão para livros
    @FXML private ComboBox<String> comboImpressora;
    @FXML private Button btnAtualizarImpressoras;
    @FXML private Button btnImprimirSelecionados;
    @FXML private ProgressIndicator progressIndicator;

    // Tabela de livros
    @FXML private TableView<LivroImpressaoDTO> tabelaLivros;
    @FXML private TableColumn<LivroImpressaoDTO, Boolean> colSelecao;
    @FXML private TableColumn<LivroImpressaoDTO, String> colSku;
    @FXML private TableColumn<LivroImpressaoDTO, String> colTitulo;
    @FXML private TableColumn<LivroImpressaoDTO, String> colTipo;
    @FXML private TableColumn<LivroImpressaoDTO, Integer> colPaginas;
    @FXML private TableColumn<LivroImpressaoDTO, String> colArquivo;
    @FXML private TableColumn<LivroImpressaoDTO, Void> colAcoes;
    @FXML private Label labelResultadosLivros;

    // Componentes de busca e filtros para coleções
    @FXML private TextField campoBuscaColecoes;
    @FXML private Button btnLimparBuscaColecoes;

    // Componentes de seleção para coleções
    @FXML private Button btnSelecionarTodasColecoes;
    @FXML private Button btnLimparSelecaoColecoes;
    @FXML private Label labelContadorColecoes;

    // Componentes de impressão para coleções
    @FXML private ComboBox<String> comboImpressoraColecao;
    @FXML private Button btnAtualizarImpressorasColecao;
    @FXML private Button btnImprimirColecoesSelecionadas;
    @FXML private ProgressIndicator progressIndicatorColecao;

    // Tabela de coleções
    @FXML private TableView<ColecaoImpressaoDTO> tabelaColecoes;
    @FXML private TableColumn<ColecaoImpressaoDTO, Boolean> colSelecaoColecao;
    @FXML private TableColumn<ColecaoImpressaoDTO, String> colSkuColecao;
    @FXML private TableColumn<ColecaoImpressaoDTO, String> colNomeColecao;
    @FXML private TableColumn<ColecaoImpressaoDTO, Integer> colQuantidadeLivros;
    @FXML private TableColumn<ColecaoImpressaoDTO, String> colNomesLivros;
    @FXML private TableColumn<ColecaoImpressaoDTO, Void> colAcoesColecao;
    @FXML private Label labelResultadosColecoes;

    private final LivroService livroService;
    private final ColecaoService colecaoService;
    private final ImpressaoService impressaoService;

    private final AuthenticationService authService;

    private final Preferences prefs = Preferences.userNodeForPackage(ImpressaoController.class);

    private ObservableList<LivroImpressaoDTO> livrosData = FXCollections.observableArrayList();
    private FilteredList<LivroImpressaoDTO> livrosFiltrados;
    private ObservableList<ColecaoImpressaoDTO> colecoesData = FXCollections.observableArrayList();
    private FilteredList<ColecaoImpressaoDTO> colecoesFiltradas;
    
    @org.springframework.beans.factory.annotation.Autowired
    public ImpressaoController(LivroService livroService, ColecaoService colecaoService, ImpressaoService impressaoService, AuthenticationService authService) {
        this.livroService = livroService;
        this.colecaoService = colecaoService;
        this.impressaoService = impressaoService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        try {
            configurarTabelaLivros();
            configurarTabelaColecoes();
            configurarFiltros();
            configurarBindings();
            carregarDados();
            atualizarImpressoras();
            restaurarPreferencias();
        } catch (Exception e) {
            log.error("Erro ao inicializar ImpressaoController", e);
            Notificacao.erro("Erro ao inicializar a aba de impressão: " + e.getMessage());
        }
    }

    private void configurarTabelaLivros() {
        colSelecao.setCellValueFactory(cellData -> cellData.getValue().selecionadoProperty());
        colSelecao.setCellFactory(tc -> new CheckBoxTableCell<>());
        
        colSku.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLivro().getSku()));
        colTitulo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLivro().getTitulo()));
        colTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLivro().getTipo().toString()));
        colPaginas.setCellValueFactory(cellData -> {
            Integer paginas = cellData.getValue().getLivro() != null ? cellData.getValue().getLivro().getPaginas() : null;
            return paginas != null ? new SimpleIntegerProperty(paginas).asObject() : new SimpleIntegerProperty(0).asObject();
        });
        colArquivo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLivro().getNomeArquivo()));

        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnVisualizar = new Button("Visualizar");
            private final HBox container = new HBox(5, btnVisualizar);

            {
                btnVisualizar.getStyleClass().add("button-secondary");
                btnVisualizar.setOnAction(e -> {
                    LivroImpressaoDTO livro = getTableRow().getItem();
                    if (livro != null && livro.getLivro() != null) {
                        String caminhoArquivo = livro.getLivro().getCaminhoPdf();
                        abrirPdfEmModal(caminhoArquivo);
                    }
                });
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        tabelaLivros.setEditable(true);
        tabelaLivros.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tabelaLivros.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configurarTabelaColecoes() {
        colSelecaoColecao.setCellValueFactory(cellData -> cellData.getValue().selecionadaProperty());
        colSelecaoColecao.setCellFactory(tc -> new CheckBoxTableCell<>());
        
        colSkuColecao.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getColecao().getSku()));
        colNomeColecao.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getColecao().getNome()));
        colQuantidadeLivros.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getColecao().getQuantidadeLivros()).asObject());
        colNomesLivros.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getColecao().getNomesLivrosTexto());
        });

        colAcoesColecao.setCellFactory(col -> new TableCell<>() {
            private final Button btnDetalhes = new Button("Detalhes");
            private final HBox container = new HBox(5, btnDetalhes);

            {
                btnDetalhes.getStyleClass().add("button-secondary");
                btnDetalhes.setOnAction(e -> {
                    ColecaoImpressaoDTO colecao = getTableRow().getItem();
                    if (colecao != null) {
                        mostrarDetalhesColecao(colecao);
                    }
                });
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        tabelaColecoes.setEditable(true);
        tabelaColecoes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tabelaColecoes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configurarFiltros() {
        livrosFiltrados = new FilteredList<>(livrosData);
        tabelaLivros.setItems(livrosFiltrados);
        
        campoBuscaLivros.textProperty().addListener((obs, old, novo) -> {
            btnLimparBuscaLivros.setVisible(!novo.isEmpty());
            livrosFiltrados.setPredicate(livro -> {
                if (novo.isEmpty()) return true;
                LivroDTO livroDTO = livro.getLivro();
                return livroDTO.getTitulo().toLowerCase().contains(novo.toLowerCase()) ||
                       livroDTO.getSku().toLowerCase().contains(novo.toLowerCase()) ||
                       livroDTO.getTipo().toString().toLowerCase().contains(novo.toLowerCase());
            });
            atualizarContadores();
        });

        colecoesFiltradas = new FilteredList<>(colecoesData);
        tabelaColecoes.setItems(colecoesFiltradas);
        
        campoBuscaColecoes.textProperty().addListener((obs, old, novo) -> {
            btnLimparBuscaColecoes.setVisible(!novo.isEmpty());
            colecoesFiltradas.setPredicate(colecao -> {
                if (novo.isEmpty()) return true;
                ColecaoResponseDTO colecaoDTO = colecao.getColecao();
                return colecaoDTO.getNome().toLowerCase().contains(novo.toLowerCase()) ||
                       colecaoDTO.getSku().toLowerCase().contains(novo.toLowerCase());
            });
            atualizarContadores();
        });
    }

    private void configurarBindings() {
        btnImprimirSelecionados.disableProperty().bind(
            Bindings.createBooleanBinding(
                () -> livrosFiltrados.stream().noneMatch(LivroImpressaoDTO::isSelecionado) ||
                      comboImpressora.getValue() == null,
                livrosFiltrados,
                comboImpressora.valueProperty()
            )
        );

        btnImprimirColecoesSelecionadas.disableProperty().bind(
            Bindings.createBooleanBinding(
                () -> colecoesFiltradas.stream().noneMatch(ColecaoImpressaoDTO::isSelecionada) ||
                      comboImpressoraColecao.getValue() == null,
                colecoesFiltradas,
                comboImpressoraColecao.valueProperty()
            )
        );
    }

    private void carregarDados() {
        try {
            List<LivroDTO> livros = livroService.listarTodos();
            List<LivroImpressaoDTO> livrosImpressao = livros.stream()
                .map(LivroImpressaoDTO::new)
                .collect(Collectors.toList());
            livrosData.setAll(livrosImpressao);
            tabelaLivros.refresh();

            if (authService.hasPermission(Permission.VIEW_COLLECTIONS)) {
                List<ColecaoDTO> colecoes = colecaoService.listarTodas();
                List<ColecaoImpressaoDTO> colecoesImpressao = colecoes.stream()
                    .map(this::converterParaColecaoResponse)
                    .map(ColecaoImpressaoDTO::new)
                    .collect(Collectors.toList());
                colecoesData.setAll(colecoesImpressao);
            }

            atualizarContadores();
        } catch (Exception e) {
            log.error("Erro ao carregar dados", e);
            Notificacao.erro("Erro ao carregar dados: " + e.getMessage());
        }
    }

    private void atualizarContadores() {
        long totalLivros = livrosFiltrados.size();
        long livrosSelecionados = livrosFiltrados.stream().filter(LivroImpressaoDTO::isSelecionado).count();
        labelContadorLivros.setText(String.format("%d de %d selecionados", livrosSelecionados, totalLivros));
        labelResultadosLivros.setText(String.format("%d livros", totalLivros));

        long totalColecoes = colecoesFiltradas.size();
        long colecoesSelecionadas = colecoesFiltradas.stream().filter(ColecaoImpressaoDTO::isSelecionada).count();
        labelContadorColecoes.setText(String.format("%d de %d selecionadas", colecoesSelecionadas, totalColecoes));
        labelResultadosColecoes.setText(String.format("%d coleções", totalColecoes));
    }

    @FXML
    public void atualizarImpressoras() {
        List<String> impressoras = ImpressoraUtil.listarImpressorasDisponiveis();
        comboImpressora.setItems(FXCollections.observableArrayList(impressoras));
        comboImpressoraColecao.setItems(FXCollections.observableArrayList(impressoras));

        String ultimaImpressora = prefs.get("ultimaImpressora", null);
        if (ultimaImpressora != null && impressoras.contains(ultimaImpressora)) {
            comboImpressora.setValue(ultimaImpressora);
            comboImpressoraColecao.setValue(ultimaImpressora);
        }
    }
    
    @FXML
    public void atualizarImpressorasColecao() {
        atualizarImpressoras();
    }

    @FXML
    public void selecionarTodosLivros() {
        boolean selecionar = !livrosFiltrados.stream().allMatch(LivroImpressaoDTO::isSelecionado);
        livrosFiltrados.forEach(livro -> livro.setSelecionado(selecionar));
        atualizarContadores();
    }

    @FXML
    public void limparSelecaoLivros() {
        livrosFiltrados.forEach(livro -> livro.setSelecionado(false));
        atualizarContadores();
    }

    @FXML
    public void selecionarTodasColecoes() {
        boolean selecionar = !colecoesFiltradas.stream().allMatch(ColecaoImpressaoDTO::isSelecionada);
        colecoesFiltradas.forEach(colecao -> colecao.setSelecionada(selecionar));
        atualizarContadores();
    }

    @FXML
    public void limparSelecaoColecoes() {
        colecoesFiltradas.forEach(colecao -> colecao.setSelecionada(false));
        atualizarContadores();
    }

    @FXML
    public void limparBuscaLivros() {
        campoBuscaLivros.clear();
    }

    @FXML
    public void limparBuscaColecoes() {
        campoBuscaColecoes.clear();
    }

    @FXML
    public void imprimirLivrosSelecionados() {
        String impressora = comboImpressora.getValue();
        List<LivroImpressaoDTO> selecionados = livrosFiltrados.stream()
            .filter(LivroImpressaoDTO::isSelecionado)
            .collect(Collectors.toList());

        if (selecionados.isEmpty()) {
            Notificacao.alerta("Nenhum livro selecionado para impressão.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText(String.format("Deseja imprimir %d livros selecionados?", selecionados.size()));
        if (alert.showAndWait().filter(r -> r == ButtonType.OK).isEmpty()) {
            return;
        }

        progressIndicator.setVisible(true);
        new Thread(() -> {
            try {
                final List<String> erros = new ArrayList<>();
                final AtomicInteger sucessos = new AtomicInteger(0);

                for (LivroImpressaoDTO livroDTO : selecionados) {
                    try {
                        if (livroDTO.getLivro() == null) {
                            erros.add("Livro selecionado está com dados inválidos");
                            continue;
                        }

                        String caminhoPdf = livroDTO.getLivro().getCaminhoPdf();
                        if (caminhoPdf == null || caminhoPdf.isEmpty()) {
                            erros.add(String.format("Caminho do PDF não definido para: %s", livroDTO.getLivro().getTitulo()));
                            continue;
                        }

                        File arquivo = new File(caminhoPdf);
                        if (!arquivo.exists() || !arquivo.isFile()) {
                            erros.add(String.format("Arquivo não encontrado: %s", livroDTO.getLivro().getTitulo()));
                            continue;
                        }

                        impressaoService.imprimir(livroDTO.getLivro().toModel(), impressora);
                        sucessos.incrementAndGet();
                    } catch (Exception e) {
                        erros.add(String.format("Erro ao imprimir %s: %s", 
                            livroDTO.getLivro() != null ? livroDTO.getLivro().getTitulo() : "livro desconhecido", 
                            e.getMessage()));
                    }
                }

                prefs.put("ultimaImpressora", impressora);

                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    if (erros.isEmpty()) {
                        Notificacao.sucesso(String.format("✅ Todos os %d livros foram impressos com sucesso!", sucessos.get()));
                    } else {
                        StringBuilder msg = new StringBuilder();
                        msg.append(String.format("⚠️ Impressão parcial:\n• Sucessos: %d livros\n• Erros: %d livros\n\n", 
                            sucessos.get(), erros.size()));
                        msg.append("Detalhes dos erros:\n");
                        erros.forEach(erro -> msg.append("• ").append(erro).append("\n"));
                        
                        if (sucessos.get() > 0) {
                            Notificacao.alerta(msg.toString());
                        } else {
                            Notificacao.erro(msg.toString());
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    Notificacao.erro("Erro ao processar impressão: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    public void imprimirColecoesSelecionadas() {
        String impressora = comboImpressoraColecao.getValue();
        List<ColecaoImpressaoDTO> selecionadas = colecoesFiltradas.stream()
            .filter(ColecaoImpressaoDTO::isSelecionada)
            .collect(Collectors.toList());

        if (selecionadas.isEmpty()) {
            Notificacao.alerta("Nenhuma coleção selecionada para impressão.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText(String.format("Deseja imprimir todas as coleções selecionadas (%d coleções)?", selecionadas.size()));
        if (alert.showAndWait().filter(r -> r == ButtonType.OK).isEmpty()) {
            return;
        }

        progressIndicatorColecao.setVisible(true);
        new Thread(() -> {
            try {
                final List<String> erros = new ArrayList<>();
                final AtomicInteger sucessos = new AtomicInteger(0);
                final AtomicInteger totalLivros = new AtomicInteger(0);

                for (ColecaoImpressaoDTO colecaoDTO : selecionadas) {
                    try {
                        Optional<Colecao> colecao = colecaoService.buscarColecaoComLivrosPorId(colecaoDTO.getColecao().getId());
                        if (colecao.isEmpty()) {
                            erros.add(String.format("Coleção não encontrada: %s", colecaoDTO.getColecao().getNome()));
                            continue;
                        }

                        ImpressaoService.ImpressaoColecaoResult resultado = 
                            impressaoService.imprimirColecao(colecao.get(), impressora);
                        
                        totalLivros.addAndGet(resultado.getTotalLivros());
                        if (resultado.tudoOk()) {
                            sucessos.incrementAndGet();
                        } else {
                            erros.addAll(resultado.getErros().stream()
                                .map(erro -> String.format("Erro em %s: %s", colecaoDTO.getColecao().getNome(), erro))
                                .collect(Collectors.toList()));
                        }
                    } catch (Exception e) {
                        erros.add(String.format("Erro ao processar coleção %s: %s", 
                            colecaoDTO.getColecao().getNome(), e.getMessage()));
                    }
                }

                prefs.put("ultimaImpressora", impressora);

                Platform.runLater(() -> {
                    progressIndicatorColecao.setVisible(false);
                    if (erros.isEmpty()) {
                        Notificacao.sucesso(String.format("✅ Todas as %d coleções foram impressas com sucesso! (%d livros)", 
                            sucessos.get(), totalLivros.get()));
                    } else {
                        StringBuilder msg = new StringBuilder();
                        msg.append(String.format("⚠️ Impressão parcial:\n• Coleções com sucesso: %d\n• Erros: %d\n\n", 
                            sucessos.get(), erros.size()));
                        msg.append("Detalhes dos erros:\n");
                        erros.forEach(erro -> msg.append("• ").append(erro).append("\n"));
                        
                        if (sucessos.get() > 0) {
                            Notificacao.alerta(msg.toString());
                        } else {
                            Notificacao.erro(msg.toString());
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressIndicatorColecao.setVisible(false);
                    Notificacao.erro("Erro ao processar impressão: " + e.getMessage());
                });
            }
        }).start();
    }

    private void mostrarDetalhesColecao(ColecaoImpressaoDTO colecao) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalhes da Coleção");
        alert.setHeaderText(colecao.getColecao().getNome());
        
        StringBuilder content = new StringBuilder();
        content.append(String.format("SKU: %s\n", colecao.getColecao().getSku()));
        content.append(String.format("Quantidade de Livros: %d\n\n", colecao.getColecao().getQuantidadeLivros()));
        content.append("Livros da Coleção:\n");
        content.append(colecao.getColecao().getNomesLivrosTexto());
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void restaurarPreferencias() {
        String ultimaImpressora = prefs.get("ultimaImpressora", null);
        if (ultimaImpressora != null) {
            List<String> impressoras = ImpressoraUtil.listarImpressorasDisponiveis();
            if (impressoras.contains(ultimaImpressora)) {
                comboImpressora.setValue(ultimaImpressora);
                comboImpressoraColecao.setValue(ultimaImpressora);
            }
        }
    }

    private void abrirPdfEmModal(String caminho) {
        if (caminho == null || caminho.isEmpty()) {
            Notificacao.erro("Arquivo não selecionado.");
            return;
        }
        File arquivo = new File(caminho);
        if (!arquivo.exists() || !arquivo.isFile()) {
            Notificacao.erro("Arquivo não encontrado: " + caminho);
            return;
        }

        try {
            WebView webView = new WebView();

            String pdfJsUrl = getClass().getResource("/web/pdfjs/web/viewer.html").toExternalForm();
            String pdfUrl = arquivo.toURI().toURL().toExternalForm();
            webView.getEngine().load(pdfJsUrl + "?file=" + pdfUrl);

            webView.setPrefSize(900, 700);

            Stage modal = ModalUtils.createModalStage("Visualizar PDF - " + arquivo.getName(), webView, null);
            modal.show();
        } catch (Exception e) {
            log.error("Erro ao abrir PDF no visualizador embutido", e);
            Notificacao.alerta("Não foi possível abrir o PDF no visualizador embutido. Tentando abrir externamente...");
            abrirPdf(caminho);
        }
    }

    private void abrirPdf(String caminho) {
        if (caminho == null || caminho.isEmpty()) {
            Notificacao.erro("Arquivo não selecionado.");
            return;
        }

        File arquivo = new File(caminho);
        if (!arquivo.exists() || !arquivo.isFile()) {
            Notificacao.erro("Arquivo não encontrado: " + caminho);
            return;
        }

        try {
            Desktop.getDesktop().open(arquivo);
        } catch (Exception e) {
            log.error("Erro ao abrir PDF externamente", e);
            Notificacao.erro("Não foi possível abrir o arquivo: " + e.getMessage());
        }
    }

    private ColecaoResponseDTO converterParaColecaoResponse(ColecaoDTO colecaoDTO) {
        Colecao colecao = colecaoService.buscarColecaoComLivrosPorId(colecaoDTO.getId())
            .orElseThrow(() -> new RuntimeException("Coleção não encontrada: " + colecaoDTO.getId()));
        
        List<String> nomesLivros = colecao.getLivros().stream()
            .map(Livro::getTitulo)
            .collect(Collectors.toList());
        
        return new ColecaoResponseDTO(
            colecao.getId(),
            colecao.getNome(),
            colecao.getSku(),
            nomesLivros
        );
    }

    @FXML
    public void recarregarDados() {
        try {
            if (authService.hasPermission(Permission.VIEW_BOOKS)) {

                List<LivroDTO> livros = livroService.listarTodos();
                livrosData.setAll(livros.stream()
                    .map(LivroImpressaoDTO::new)
                    .collect(Collectors.toList()));

                List<ColecaoDTO> colecoes = colecaoService.listarTodas();
                colecoesData.setAll(colecoes.stream()
                    .map(this::converterParaColecaoResponse)
                    .map(ColecaoImpressaoDTO::new)
                    .collect(Collectors.toList()));

                atualizarContadores();
            } else {
                livrosData.clear();
                colecoesData.clear();
                Notificacao.alerta("Você não tem permissão para visualizar livros.");
            }

            if (authService.hasPermission(Permission.VIEW_PRINTERS)) {
                atualizarImpressoras();
            } else {
                comboImpressora.setItems(FXCollections.observableArrayList());
                comboImpressoraColecao.setItems(FXCollections.observableArrayList());
                Notificacao.alerta("Você não tem permissão para visualizar impressoras.");
            }
        } catch (Exception e) {
            log.error("Erro ao recarregar dados da aba Impressão", e);
            Notificacao.erro("Erro ao carregar dados da aba Impressão.");
        }
    }
}