package com.diagonal.cordeis.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

public class ModalInformativo {

    public static void mostrarInformacoesSistema() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("📖 Como Funciona o Sistema");
        alert.setHeaderText("Sistema Inteligente de Importação de Livros");
        
        // Criar conteúdo detalhado
        StringBuilder conteudo = new StringBuilder();
        conteudo.append("🤖 DETECÇÃO AUTOMÁTICA DE SKU E TIPO:\n\n");
        
        conteudo.append("📋 FORMATOS SUPORTADOS:\n");
        conteudo.append("• MC0001_TituloDoLivro.pdf → Mini Cordel (SKU: MC0001)\n");
        conteudo.append("• FC0001_TituloDoLivro.pdf → Folheto Cordel (SKU: FC0001)\n");
        conteudo.append("• historia_sem_sku.pdf → Gera SKU automaticamente\n\n");
        
        conteudo.append("🔄 PROCESSO AUTOMÁTICO:\n");
        conteudo.append("1. Sistema analisa o nome do arquivo\n");
        conteudo.append("2. Extrai SKU (MC#### ou FC####) se presente\n");
        conteudo.append("3. Determina tipo: MC = Mini, FC = Folheto\n");
        conteudo.append("4. Limpa e formata o título automaticamente\n");
        conteudo.append("5. Gera SKU sequencial se não detectado\n\n");
        
        conteudo.append("✨ FUNCIONALIDADES INTELIGENTES:\n");
        conteudo.append("• Remove códigos e caracteres especiais do título\n");
        conteudo.append("• Capitaliza primeira letra de cada palavra\n");
        conteudo.append("• Garante SKUs únicos no sistema\n");
        conteudo.append("• Numeração sequencial por tipo (MC0001, MC0002...)\n\n");
        
        conteudo.append("📝 COMO USAR:\n");
        conteudo.append("1. Organize seus PDFs com nomes descritivos\n");
        conteudo.append("2. Use padrão MC#### ou FC#### no nome (opcional)\n");
        conteudo.append("3. Selecione a pasta com os PDFs\n");
        conteudo.append("4. Revise os títulos detectados na tabela\n");
        conteudo.append("5. Clique em 'Importar Livros'\n\n");
        
        conteudo.append("🎯 CAPA INTEGRADA:\n");
        conteudo.append("• PDFs devem ter a capa na primeira página\n");
        conteudo.append("• Sistema imprime o livro completo\n");
        conteudo.append("• Não precisa de arquivos de capa separados\n\n");
        
        conteudo.append("⚠️ DICAS IMPORTANTES:\n");
        conteudo.append("• Organize PDFs em pastas por categoria\n");
        conteudo.append("• Use nomes descritivos nos arquivos\n");
        conteudo.append("• Verifique se PDFs não estão corrompidos\n");
        conteudo.append("• Títulos podem ser editados após detecção\n");

        // Criar TextArea com scroll para o conteúdo
        TextArea textArea = new TextArea(conteudo.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        // Aplicar estilo
        textArea.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-radius: 5px; " +
            "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #333;"
        );

        // Configurar container
        VBox container = new VBox(textArea);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        container.setMaxWidth(Double.MAX_VALUE);
        container.setMaxHeight(Double.MAX_VALUE);

        // Configurar ScrollPane
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefSize(600, 500);
        
        alert.getDialogPane().setContent(scrollPane);
        alert.getDialogPane().setPrefSize(650, 550);
        
        // Configurar estilo do modal
        alert.initStyle(StageStyle.UTILITY);
        alert.setResizable(true);
        
        alert.showAndWait();
    }

    public static void mostrarInstrucoesRapidas() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🚀 Início Rápido");
        alert.setHeaderText("Como importar livros em 4 passos");
        
        String conteudo = 
            "1️⃣ SELECIONAR PASTA\n" +
            "   Clique no botão 📁 e escolha a pasta com seus PDFs\n\n" +
            
            "2️⃣ VERIFICAR DETECÇÃO\n" +
            "   Sistema mostra SKU e tipo detectados automaticamente\n\n" +
            
            "3️⃣ REVISAR TÍTULOS\n" +
            "   Clique duas vezes nos títulos para editá-los se necessário\n\n" +
            
            "4️⃣ IMPORTAR\n" +
            "   Clique em 'Importar Livros' para finalizar\n\n" +
            
            "💡 DICA: Use nomes como 'MC0001_Historia_Lampiao.pdf' para melhor detecção!";

        alert.setContentText(conteudo);
        alert.getDialogPane().setPrefSize(500, 350);
        alert.showAndWait();
    }
}
