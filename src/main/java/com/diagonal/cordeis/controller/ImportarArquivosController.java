package com.diagonal.cordeis.controller;

import com.diagonal.cordeis.dto.ArquivoImportadoDTO;
import com.diagonal.cordeis.dto.LivroDTO;
import com.diagonal.cordeis.model.Livro;
import com.diagonal.cordeis.service.LivroService;
import com.diagonal.cordeis.util.ArquivoUtils;
import com.diagonal.cordeis.util.LivroUtils;
import com.diagonal.cordeis.view.Notificacao;
import com.diagonal.cordeis.view.ModalInformativo;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ImportarArquivosController {

    @FXML private TextField diretorioField;
    @FXML private Label labelResumo;

    @FXML private TableView<ArquivoImportadoDTO> tabelaArquivos;
    @FXML private TableColumn<ArquivoImportadoDTO, String> colTitulo;
    @FXML private TableColumn<ArquivoImportadoDTO, String> colCaminho;
    @FXML private TableColumn<ArquivoImportadoDTO, String> colTipo;
    @FXML private Button btnVincularCapasEmLote;

    private final LivroService livroService;

    public ImportarArquivosController(LivroService livroService) {
        this.livroService = livroService;
    }

    @FXML
    public void initialize() {
        // Inicializar contador de livros
        atualizarContadorLivros();
        
        colTitulo.setCellValueFactory(cellData -> cellData.getValue().tituloProperty());
        colCaminho.setCellValueFactory(cellData -> cellData.getValue().caminhoProperty());
        colTipo.setCellValueFactory(cellData -> cellData.getValue().tipoProperty());

        tabelaArquivos.setEditable(true);
        colTitulo.setCellFactory(TextFieldTableCell.forTableColumn());
        colTitulo.setOnEditCommit(event -> {
            event.getRowValue().setTitulo(event.getNewValue());
            // Feedback visual de que o título foi alterado
            HBox resumoBox = new HBox(5);
            resumoBox.setAlignment(Pos.CENTER_LEFT);
            FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
            editIcon.setFill(Color.valueOf("#2196F3"));
            Label texto = new Label("Título alterado • " + tabelaArquivos.getItems().size() + " arquivos prontos");
            resumoBox.getChildren().addAll(editIcon, texto);
            labelResumo.setGraphic(resumoBox);
        });
        
        // Inicialização completa
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        FontAwesomeIconView readyIcon = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
        readyIcon.setFill(Color.valueOf("#4CAF50"));
        Label statusText = new Label("Sistema pronto para importar");
        statusBox.getChildren().addAll(readyIcon, statusText);
        labelResumo.setGraphic(statusBox);
    }
    
    private void atualizarContadorLivros() {
        try {
            long totalLivros = livroService.contarTodos();
            labelResumo.setText("📚 Total de livros no sistema: " + totalLivros + " • Pronto para importar");
        } catch (Exception e) {
            labelResumo.setText("📚 Sistema pronto para importar");
        }
    }
    
    @FXML
    public void escolherDiretorio() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecionar Diretório com PDFs");
        File dir = chooser.showDialog(null);
        if (dir != null) {
            diretorioField.setText(dir.getAbsolutePath());
            carregarArquivos(dir);
        }
    }

    private void carregarArquivos(File diretorio) {
        File[] arquivos = diretorio.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));
        if (arquivos == null || arquivos.length == 0) {
            tabelaArquivos.setItems(FXCollections.emptyObservableList());
            labelResumo.setText("📂 Nenhum PDF encontrado na pasta selecionada");
            return;
        }

        List<ArquivoImportadoDTO> lista = new ArrayList<>();
        int arquivosValidos = 0;
        int arquivosInvalidos = 0;
        int totalPaginas = 0;

        // Feedback durante processamento
        labelResumo.setText("🔍 Analisando " + arquivos.length + " arquivos...");

        for (File pdf : arquivos) {
            String caminho = pdf.getAbsolutePath();

            if (!ArquivoUtils.validarPdf(caminho)) {
                arquivosInvalidos++;
                continue;
            }

            String nomeArquivo = ArquivoUtils.extrairNomeArquivo(caminho);
            int paginas = ArquivoUtils.contarPaginasPdf(caminho);
            totalPaginas += paginas;

            // Extrair informações automaticamente do nome do arquivo
            String skuDetectado = LivroUtils.extrairSku(nomeArquivo);
            String tituloLimpo = LivroUtils.extrairTitulo(nomeArquivo);
            String tipoDetectado = LivroUtils.determinarTipoPorNome(nomeArquivo).toString();

            ArquivoImportadoDTO dto = new ArquivoImportadoDTO();
            dto.setCaminho(caminho);
            dto.setTitulo(tituloLimpo.isEmpty() ? nomeArquivo.replace(".pdf", "") : tituloLimpo);
            dto.setTipo(tipoDetectado);
            dto.setPaginas(paginas);

            // Adicionar informação do SKU se detectado
            if (skuDetectado != null && !skuDetectado.isEmpty()) {
                dto.setTitulo(dto.getTitulo() + " [SKU: " + skuDetectado + "]");
            }

            lista.add(dto);
            arquivosValidos++;
        }

        tabelaArquivos.setItems(FXCollections.observableArrayList(lista));
        
        // Criar resumo mais informativo e detalhado
        StringBuilder resumo = new StringBuilder();
        resumo.append("✅ ").append(arquivosValidos).append(" PDFs válidos • ");
        resumo.append("📄 ").append(totalPaginas).append(" páginas totais");
        if (arquivosInvalidos > 0) {
            resumo.append(" • ❌ ").append(arquivosInvalidos).append(" inválidos");
        }
        labelResumo.setText(resumo.toString());
    }

    @FXML
    public void confirmarImportacao() throws IOException {
        ObservableList<ArquivoImportadoDTO> lista = tabelaArquivos.getItems();

        if (lista.isEmpty()) {
            Notificacao.informacao("📂 Nenhum arquivo para importar.\n\nSelecione uma pasta com PDFs primeiro.");
            return;
        }

        // Validação prévia com feedback detalhado
        for (ArquivoImportadoDTO dto : lista) {
            if (dto.getTitulo() == null || dto.getTitulo().isBlank()) {
                Notificacao.erro("❌ Título inválido encontrado:\n\n📁 " + dto.getCaminho() + 
                               "\n\n💡 Clique duas vezes no título para editá-lo.");
                return;
            }
        }

        // Feedback visual durante importação
        int totalArquivos = lista.size();
        labelResumo.setText("⏳ Importando " + totalArquivos + " livros...");

        int sucessos = 0;
        int falhas = 0;
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("=== RELATÓRIO DE IMPORTAÇÃO ===\n\n");

        for (ArquivoImportadoDTO dto : lista) {
            try {
                // Criar livro com informações básicas - SKU e tipo serão determinados automaticamente
                Livro livro = new Livro();
                livro.setCaminhoPdf(dto.getCaminho());
                // Não definir título aqui - será extraído automaticamente do nome do arquivo
                // Não definir tipo aqui - será determinado automaticamente pelo SKU
                
                livroService.salvar(LivroDTO.fromModel(livro));
                sucessos++;
                
                // Usar o título que foi processado automaticamente
                String tituloFinal = livro.getTitulo() != null ? livro.getTitulo() : dto.getTitulo();
                String skuFinal = livro.getSku() != null ? " (SKU: " + livro.getSku() + ")" : "";
                relatorio.append("✅ ").append(tituloFinal).append(skuFinal).append("\n");
                
                // Atualizar feedback em tempo real
                labelResumo.setText("⏳ Importando... " + sucessos + "/" + totalArquivos + " concluídos");
                
            } catch (Exception e) {
                falhas++;
                relatorio.append("❌ ").append(dto.getTitulo()).append(" - Erro: ").append(e.getMessage()).append("\n");
            }
        }

        relatorio.append("\n📊 RESUMO FINAL:\n");
        relatorio.append("✅ Sucessos: ").append(sucessos).append("\n");
        relatorio.append("❌ Falhas: ").append(falhas).append("\n");

        // Feedback final
        if (falhas == 0) {
            labelResumo.setText("🎉 Importação concluída! " + sucessos + " livros adicionados com sucesso");
            Notificacao.sucesso("🎉 Importação concluída com sucesso!\n\n📚 " + sucessos + 
                               " livros foram adicionados ao sistema.");
        } else {
            labelResumo.setText("⚠️ Importação concluída com problemas: " + sucessos + " sucessos, " + falhas + " falhas");
            Notificacao.alerta("⚠️ Importação concluída com problemas:\n\n✅ " + sucessos + 
                              " sucessos\n❌ " + falhas + " falhas\n\n📋 Verifique o log para detalhes.");
        }

        // Atualizar contador
        atualizarContadorLivros();
    }

    @FXML
    public void fechar() {
        Stage stage = (Stage) tabelaArquivos.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void mostrarInformacoesSistema() {
        ModalInformativo.mostrarInformacoesSistema();
    }

    @FXML
    public void cancelarImportacao() {
        diretorioField.clear();
        tabelaArquivos.setItems(FXCollections.emptyObservableList());
        
        // Atualizar contador
        atualizarContadorLivros();

        Notificacao.informacao("🧹 Formulário de importação limpo.\n\nPronto para uma nova importação!");
    }

}
