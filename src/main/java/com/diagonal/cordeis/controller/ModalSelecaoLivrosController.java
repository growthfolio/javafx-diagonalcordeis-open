package com.diagonal.cordeis.controller;

import com.diagonal.cordeis.dto.LivroDTO;
import com.diagonal.cordeis.service.LivroService;
import com.diagonal.cordeis.view.Notificacao;
import com.diagonal.cordeis.view.SpringContext;
import com.diagonal.cordeis.view.UXUtils;
import com.diagonal.cordeis.view.AdvancedSearchComponent;
import com.diagonal.cordeis.view.KeyboardShortcutManager;
import com.diagonal.cordeis.view.ModalUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Component
public class ModalSelecaoLivrosController {

    private LivroService livroService;

    // COMPONENTES FXML
    @FXML private Label labelNomeColecao;
    @FXML private ComboBox<String> comboFiltroTipo;
    @FXML private TextField campoBuscaModal;
    @FXML private Label labelContadorFiltrados;
    @FXML private Label labelTotalDisponiveis;
    @FXML private Label labelContadorSelecionados;

    @FXML private TableView<LivroDTO> tabelaLivrosDisponiveis;
    @FXML private TableColumn<LivroDTO, String> colTitulo;
    @FXML private TableColumn<LivroDTO, String> colSku;
    @FXML private TableColumn<LivroDTO, String> colTipo;
    @FXML private TableColumn<LivroDTO, Boolean> colSelecionar;

    @FXML private ListView<LivroDTO> listSelecionados;

    @FXML private Button btnCancelar;
    @FXML private Button btnConfirmar;

    // DADOS
    private final ObservableList<LivroDTO> todosLivros = FXCollections.observableArrayList();
    private final ObservableList<LivroDTO> livrosDisponiveis = FXCollections.observableArrayList();
    private final ObservableList<LivroDTO> livrosSelecionados = FXCollections.observableArrayList();
    
    // Map para rastrear os checkboxes e seus estados
    private final Map<LivroDTO, CheckBox> checkBoxMap = new HashMap<>();

    // CALLBACKS
    private Consumer<List<LivroDTO>> onConfirmar;
    private Runnable onCancelar;

    // STAGE
    private Stage modalStage;
    
    // NOVOS COMPONENTES UX
    private AdvancedSearchComponent<LivroDTO> advancedSearch;
    private KeyboardShortcutManager shortcutManager;

    @FXML
    public void initialize() {
        log.info("Inicializando modal de seleção de livros com melhorias UX");
        
        // Obter o LivroService via SpringContext
        try {
            livroService = SpringContext.getBean(LivroService.class);
        } catch (Exception e) {
            log.error("Erro ao obter LivroService do contexto Spring", e);
            Notificacao.erro("Erro ao inicializar modal: " + e.getMessage());
            return;
        }
        
        configurarComponentes();
        configurarEventos();
        configurarTooltips();
        setupAdvancedSearch();
        carregarDados();
        
        // Aplicar melhorias visuais
        Platform.runLater(this::applyUXEnhancements);
    }
    
    /**
     * Configura o sistema de pesquisa avançada
     */
    private void setupAdvancedSearch() {
        // Criar componente de pesquisa avançada
        advancedSearch = new AdvancedSearchComponent<>(todosLivros);
        advancedSearch.setSearchPrompt("Pesquisar livros por título, SKU ou tipo...");
        
        // Configurar provedor de pesquisa personalizado
        advancedSearch.setSearchProvider((livro, searchText) -> {
            String search = searchText.toLowerCase();
            return livro.getTitulo().toLowerCase().contains(search) ||
                   livro.getSku().toLowerCase().contains(search) ||
                   livro.getTipo().toString().toLowerCase().contains(search);
        });
        
        // Adicionar filtros
        advancedSearch.addFilter("tipo_normal", "Normal", 
            livro -> "NORMAL".equals(livro.getTipo().toString()));
        advancedSearch.addFilter("tipo_mini", "Mini",
            livro -> "MINI".equals(livro.getTipo().toString()));
        advancedSearch.addFilter("ja_selecionados", "Já Selecionados",
            livro -> livrosSelecionados.contains(livro));
        
        // Conectar com a tabela
        tabelaLivrosDisponiveis.setItems(advancedSearch.getFilteredList());
    }
    
    /**
     * Aplica melhorias UX ao modal
     */
    private void applyUXEnhancements() {
        // Aplicar efeitos visuais aos botões
        UXUtils.applyHoverEffect(btnConfirmar);
        UXUtils.applyHoverEffect(btnCancelar);
        
        // Aplicar validação em tempo real ao campo de busca
        UXUtils.applyRealTimeValidation(campoBuscaModal, text -> {
            if (text == null || text.trim().isEmpty()) {
                return UXUtils.ValidationResult.success();
            }
            
            long matches = todosLivros.stream()
                .filter(livro -> livro.getTitulo().toLowerCase().contains(text.toLowerCase()) ||
                               livro.getSku().toLowerCase().contains(text.toLowerCase()))
                .count();
            
            if (matches == 0) {
                return UXUtils.ValidationResult.warning("Nenhum livro encontrado");
            }
            
            return UXUtils.ValidationResult.success();
        });
        
        // Aplicar microinterações nas listas
        UXUtils.applyMicroInteractions(tabelaLivrosDisponiveis);
        UXUtils.applyMicroInteractions(listSelecionados);
        
        // Configurar atalhos de teclado
        setupKeyboardShortcuts();
        
        // Aplicar transições suaves
        UXUtils.fadeIn(tabelaLivrosDisponiveis);
        UXUtils.slideUp(listSelecionados);
    }
    
    /**
     * Configura atalhos de teclado para o modal
     */
    private void setupKeyboardShortcuts() {
        if (modalStage != null && modalStage.getScene() != null) {
            shortcutManager = new KeyboardShortcutManager(modalStage.getScene());
            
            // Configurar atalhos específicos do modal
            shortcutManager.addCommonShortcuts(KeyboardShortcutManager.ShortcutContext.MODAL_DIALOG);
            
            // Atalhos personalizados
            var componentShortcuts = new KeyboardShortcutManager.ComponentShortcuts();
            componentShortcuts.add(KeyCode.SPACE, this::toggleSelectedItem);
            componentShortcuts.add(KeyCode.DELETE, this::removeSelectedItems);
            componentShortcuts.add(KeyCode.A, KeyCombination.CONTROL_DOWN, this::selectAllItems);
            
            KeyboardShortcutManager.addComponentShortcuts(tabelaLivrosDisponiveis, componentShortcuts);
        }
    }
    
    /**
     * Alterna seleção do item atual na tabela
     */
    private void toggleSelectedItem() {
        LivroDTO selected = tabelaLivrosDisponiveis.getSelectionModel().getSelectedItem();
        if (selected != null) {
            CheckBox checkBox = checkBoxMap.get(selected);
            if (checkBox != null) {
                checkBox.setSelected(!checkBox.isSelected());
                UXUtils.pulseAttention(tabelaLivrosDisponiveis);
            }
        }
    }
    
    /**
     * Remove itens selecionados
     */
    private void removeSelectedItems() {
        List<LivroDTO> selectedItems = new ArrayList<>(listSelecionados.getSelectionModel().getSelectedItems());
        for (LivroDTO livro : selectedItems) {
            removerLivro(livro);
        }
        
        if (!selectedItems.isEmpty()) {
            UXUtils.showToast(listSelecionados, 
                selectedItems.size() + " item(s) removido(s)", 
                UXUtils.ToastType.INFO, 
                javafx.util.Duration.seconds(2));
        }
    }
    
    /**
     * Seleciona todos os itens visíveis
     */
    private void selectAllItems() {
        for (LivroDTO livro : advancedSearch.getFilteredList()) {
            if (!livrosSelecionados.contains(livro)) {
                livrosSelecionados.add(livro);
                CheckBox checkBox = checkBoxMap.get(livro);
                if (checkBox != null) {
                    checkBox.setSelected(true);
                }
            }
        }
        
        tabelaLivrosDisponiveis.refresh();
        atualizarContadores();
        
        UXUtils.showToast(tabelaLivrosDisponiveis,
            "Todos os itens visíveis foram selecionados",
            UXUtils.ToastType.SUCCESS,
            javafx.util.Duration.seconds(2));
    }

    private void configurarTooltips() {
        btnCancelar.setTooltip(new Tooltip("Cancelar e fechar (Esc)"));
        btnConfirmar.setTooltip(new Tooltip("Confirmar seleção de livros (Enter)"));
        
        // Tooltips melhorados com informações de atalhos
        campoBuscaModal.setTooltip(new Tooltip("Digite para filtrar livros\nUse Ctrl+A para selecionar todos"));
        tabelaLivrosDisponiveis.setTooltip(new Tooltip("Clique duplo ou Space para selecionar\nCtrl+A para selecionar todos visíveis"));
        listSelecionados.setTooltip(new Tooltip("Duplo clique ou Delete para remover"));
    }

    private void configurarComponentes() {
        // Configurar tabela
        tabelaLivrosDisponiveis.setItems(livrosDisponiveis);
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        
        // Tornar tabela editável
        tabelaLivrosDisponiveis.setEditable(true);
        
        // Configurar edição inline para título
        colTitulo.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        colTitulo.setOnEditCommit(event -> {
            LivroDTO livro = event.getRowValue();
            String novoTitulo = event.getNewValue();
            
            try {
                livroService.atualizarTitulo(livro.getId(), novoTitulo);
                livro.setTitulo(novoTitulo);
                
                // Feedback visual compacto para modal
                log.info("Título atualizado no modal: {} -> {}", livro.getSku(), novoTitulo);
                
            } catch (Exception e) {
                // Reverter em caso de erro
                event.getTableView().refresh();
                log.error("Erro ao atualizar título no modal: {}", e.getMessage());
            }
        });
        
        // Configurar coluna de seleção com checkboxes melhorados
        colSelecionar.setCellFactory(col -> new TableCell<LivroDTO, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            
            {
                // Centralize o checkbox na célula
                setAlignment(javafx.geometry.Pos.CENTER);
                
                checkBox.setOnAction(event -> {
                    LivroDTO livro = getTableRow().getItem();
                    if (livro != null) {
                        if (checkBox.isSelected()) {
                            if (!livrosSelecionados.contains(livro)) {
                                livrosSelecionados.add(livro);
                                UXUtils.showToast(getTableRow(),
                                    "\"" + livro.getTitulo() + "\" adicionado",
                                    UXUtils.ToastType.SUCCESS,
                                    javafx.util.Duration.seconds(1));
                            }
                        } else {
                            livrosSelecionados.remove(livro);
                            UXUtils.showToast(getTableRow(),
                                "\"" + livro.getTitulo() + "\" removido",
                                UXUtils.ToastType.INFO,
                                javafx.util.Duration.seconds(1));
                        }
                        checkBoxMap.put(livro, checkBox);
                        atualizarContadores();
                    }
                    event.consume();
                });
                
                // Estilize o checkbox para melhor visibilidade
                checkBox.setStyle("-fx-padding: 0 5 0 5;");
            }
            
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    LivroDTO livro = getTableRow().getItem();
                    if (livro != null) {
                        checkBox.setSelected(livrosSelecionados.contains(livro));
                        checkBoxMap.put(livro, checkBox);
                        
                        // Adicione estilo à célula para destacar o checkbox
                        if (checkBox.isSelected()) {
                            setStyle("-fx-background-color: #E3F2FD;");
                        } else {
                            setStyle("");
                        }
                    }
                    setGraphic(checkBox);
                }
            }
        });
        
        // Adicionar listener para atualizar estado dos checkboxes
        livrosSelecionados.addListener((javafx.collections.ListChangeListener<LivroDTO>) c -> {
            tabelaLivrosDisponiveis.refresh();
        });

        // Permitir seleção múltipla na tabela
        tabelaLivrosDisponiveis.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Configurar lista de selecionados
        listSelecionados.setItems(livrosSelecionados);
        listSelecionados.setCellFactory(this::criarCellFactory);
        listSelecionados.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Configurar ComboBoxes
        comboFiltroTipo.setItems(FXCollections.observableArrayList("TODOS", "NORMAL", "MINI"));
        comboFiltroTipo.setValue("TODOS");
    }

    private void configurarEventos() {
        // Listeners para filtros com feedback visual melhorado
        campoBuscaModal.textProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                filtrarLivros();
                
                // Aplicar feedback visual na pesquisa
                if (newVal != null && !newVal.trim().isEmpty()) {
                    long resultados = livrosDisponiveis.size();
                    if (resultados == 0) {
                        UXUtils.animateBackgroundColor(campoBuscaModal, "#FFFFFF", "#FFEBEE", javafx.util.Duration.millis(300));
                    } else {
                        UXUtils.animateBackgroundColor(campoBuscaModal, "#FFEBEE", "#FFFFFF", javafx.util.Duration.millis(300));
                    }
                }
            });
        });
        
        comboFiltroTipo.valueProperty().addListener((obs, oldVal, newVal) -> {
            filtrarLivros();
            UXUtils.pulseAttention(comboFiltroTipo);
        });

        // Listeners para contadores com animações
        livrosSelecionados.addListener((javafx.collections.ListChangeListener<LivroDTO>) c -> {
            atualizarContadores();
            // Aplicar efeito visual quando itens são adicionados/removidos
            if (c.next() && c.wasAdded()) {
                c.getAddedSubList().forEach(livro -> 
                    UXUtils.pulseAttention(listSelecionados)
                );
            }
        });

        // Duplo clique na tabela para alternar o checkbox com feedback visual melhorado
        tabelaLivrosDisponiveis.setRowFactory(tv -> {
            TableRow<LivroDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    LivroDTO livro = row.getItem();
                    CheckBox checkBox = checkBoxMap.get(livro);
                    if (checkBox != null) {
                        // Inverte o estado do checkbox
                        checkBox.setSelected(!checkBox.isSelected());
                        // Aplicar feedback visual
                        UXUtils.pulseAttention(row);
                    }
                }
            });
            
            // Adicionar hover effect melhorado
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #F3F9FF;");
                }
            });
            
            row.setOnMouseExited(e -> {
                if (!row.isEmpty() && !row.isSelected()) {
                    row.setStyle("");
                }
            });
            
            return row;
        });

        // Duplo clique na lista para remover com confirmação visual
        listSelecionados.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                LivroDTO selecionado = listSelecionados.getSelectionModel().getSelectedItem();
                if (selecionado != null) {
                    // Aplicar feedback visual na lista
                    UXUtils.pulseAttention(listSelecionados);
                    Platform.runLater(() -> removerLivro(selecionado));
                }
            }
        });
    }

    private ListCell<LivroDTO> criarCellFactory(ListView<LivroDTO> listView) {
        return new ListCell<LivroDTO>() {
            @Override
            protected void updateItem(LivroDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("📖 %s (%s)", 
                        item.getTitulo(), 
                        item.getSku()));
                    
                    String cor = item.getTipo().toString().equals("MINI") ? "#F3E5F5" : "#E8F5E8";
                    setStyle("-fx-background-color: " + cor + "; -fx-background-radius: 6; -fx-padding: 6; -fx-border-color: #DEDEDE; -fx-border-radius: 6; -fx-font-size: 12px;");
                    
                    // Adicionar tooltip para mostrar informações completas
                    setTooltip(new Tooltip(String.format(
                        "Título: %s\nSKU: %s\nTipo: %s\n\nDuplo clique para remover", 
                        item.getTitulo(), 
                        item.getSku(), 
                        item.getTipo())));
                }
            }
        };
    }

    private void carregarDados() {
        // Mostrar loading com mensagem personalizada
        UXUtils.executeWithLoading(
            tabelaLivrosDisponiveis,
            "Carregando livros disponíveis...",
            () -> {
                try {
                    return livroService.listarTodos();
                } catch (Exception e) {
                    log.error("Erro ao carregar livros", e);
                    throw new RuntimeException("Erro ao carregar livros: " + e.getMessage());
                }
            },
            livros -> {
                todosLivros.setAll(livros);
                filtrarLivros();
                log.info("Carregados {} livros para seleção", livros.size());
            },
            error -> {
                log.error("Erro ao carregar livros", error);
                Notificacao.erro("Erro ao carregar livros: " + error.getMessage());
            }
        );
    }

    private void filtrarLivros() {
        String textoBusca = campoBuscaModal.getText();
        String tipoFiltro = comboFiltroTipo.getValue();

        List<LivroDTO> livrosFiltrados = todosLivros.stream()
                .filter(livro -> {
                    // Filtro por texto
                    boolean matchTexto = textoBusca == null || textoBusca.trim().isEmpty() ||
                            livro.getTitulo().toLowerCase().contains(textoBusca.toLowerCase()) ||
                            livro.getSku().toLowerCase().contains(textoBusca.toLowerCase());

                    // Filtro por tipo
                    boolean matchTipo = "TODOS".equals(tipoFiltro) || 
                            livro.getTipo().toString().equals(tipoFiltro);

                    return matchTexto && matchTipo;
                })
                .toList();

        livrosDisponiveis.setAll(livrosFiltrados);
        atualizarContadores();
        
        // Agenda uma atualização da UI para garantir que os checkboxes estejam corretamente marcados
        Platform.runLater(() -> tabelaLivrosDisponiveis.refresh());
    }

    private void atualizarContadores() {
        int filtrados = livrosDisponiveis.size();
        int selecionados = livrosSelecionados.size();
        int total = todosLivros.size();

        labelContadorFiltrados.setText(filtrados + " livros");
        labelTotalDisponiveis.setText("(" + filtrados + " de " + total + ")");
        labelContadorSelecionados.setText(String.valueOf(selecionados));
        
        // Aplicar animações nos contadores
        if (selecionados > 0) {
            UXUtils.pulseAttention(labelContadorSelecionados);
        }
    }

    // AÇÕES DOS BOTÕES
    @FXML
    public void cancelar() {
        log.info("Cancelando seleção de livros");
        if (onCancelar != null) {
            onCancelar.run();
        }
        fecharModal();
    }

    @FXML
    public void confirmar() {
        log.info("Confirmando seleção de {} livros", livrosSelecionados.size());
        
        if (livrosSelecionados.isEmpty()) {
            UXUtils.showToast(btnConfirmar,
                "Selecione pelo menos um livro",
                UXUtils.ToastType.WARNING,
                javafx.util.Duration.seconds(3));
            return;
        }
        
        // Aplicar loading no botão
        UXUtils.setButtonLoading(btnConfirmar, true);
        
        Platform.runLater(() -> {
            try {
                if (onConfirmar != null) {
                    onConfirmar.accept(new ArrayList<>(livrosSelecionados));
                }
                
                UXUtils.showToast(btnConfirmar,
                    livrosSelecionados.size() + " livro(s) confirmado(s)",
                    UXUtils.ToastType.SUCCESS,
                    javafx.util.Duration.seconds(2));
                
                // Aguardar um pouco antes de fechar para mostrar o feedback
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                pause.setOnFinished(e -> fecharModal());
                pause.play();
                
            } catch (Exception e) {
                log.error("Erro ao confirmar seleção", e);
                UXUtils.showToast(btnConfirmar,
                    "Erro ao confirmar seleção",
                    UXUtils.ToastType.ERROR,
                    javafx.util.Duration.seconds(3));
            } finally {
                UXUtils.setButtonLoading(btnConfirmar, false);
            }
        });
    }

    // MÉTODOS AUXILIARES
    private void removerLivro(LivroDTO livro) {
        livrosSelecionados.remove(livro);
        CheckBox checkBox = checkBoxMap.get(livro);
        if (checkBox != null) {
            checkBox.setSelected(false);
        }
        tabelaLivrosDisponiveis.refresh();
        atualizarContadores();
        
        log.debug("Livro removido da seleção: {}", livro.getTitulo());
    }

    private void fecharModal() {
        if (modalStage != null) {
            // Aplicar transição de fechamento suave com fade-out
            UXUtils.fadeOut(modalStage.getScene().getRoot(), javafx.util.Duration.millis(300), () -> {
                modalStage.close();
            });
        }
    }

    // MÉTODOS PÚBLICOS PARA CONFIGURAÇÃO

    public void setNomeColecao(String nome) {
        if (labelNomeColecao != null) {
            labelNomeColecao.setText(nome);
            UXUtils.fadeIn(labelNomeColecao);
        }
    }

    public void setLivrosJaSelecionados(List<LivroDTO> livrosSelecionados) {
        this.livrosSelecionados.setAll(livrosSelecionados);
        atualizarContadores();
    }

    public void setOnConfirmar(Consumer<List<LivroDTO>> callback) {
        this.onConfirmar = callback;
    }

    public void setOnCancelar(Runnable callback) {
        this.onCancelar = callback;
    }

    public void setModalStage(Stage stage) {
        this.modalStage = stage;
        
        // Configurar dimensionamento automático para o modal
        ModalUtils.configureModalSizing(stage, stage.getScene().getRoot());
        
        // Configurar atalhos quando o stage estiver disponível
        Platform.runLater(this::setupKeyboardShortcuts);
    }
}
