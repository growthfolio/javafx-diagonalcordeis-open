package com.diagonal.cordeis.view;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import java.util.Optional;

public class CaixaDeDialogo {

    public static void erro(String titulo, String mensagem) {
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.TIMES_CIRCLE);
        icon.setSize("32");
        exibirComIcone(Alert.AlertType.ERROR, titulo, mensagem, icon, "#e53935");
    }

    public static void alerta(String titulo, String mensagem) {
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
        icon.setSize("32");
        exibirComIcone(Alert.AlertType.WARNING, titulo, mensagem, icon, "#fbc02d");
    }

    public static void informacao(String titulo, String mensagem) {
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.INFO_CIRCLE);
        icon.setSize("32");
        exibirComIcone(Alert.AlertType.INFORMATION, titulo, mensagem, icon, "#1e88e5");
    }

    public static boolean confirmar(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.getDialogPane().setStyle("-fx-background-color: #23272f; -fx-border-radius: 12px; -fx-background-radius: 12px;");
        alert.getDialogPane().lookup(".content.label").setStyle("-fx-font-size: 15px; -fx-text-fill: white;");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getScene().getRoot().setStyle("-fx-font-family: 'Segoe UI', sans-serif;");
        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.YES;
    }

    private static void exibirComIcone(Alert.AlertType tipo, String titulo, String mensagem, Node icone, String cor) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        // Configurar ícone FontAwesome
        if (icone instanceof FontAwesomeIconView) {
            ((FontAwesomeIconView) icone).setFill(Color.valueOf(cor));
        }
        alert.setGraphic(icone);

        // Estilo moderno
        alert.getDialogPane().setStyle(
            "-fx-background-color: #23272f; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px; " +
            "-fx-border-color: " + cor + "; " +
            "-fx-border-width: 3px;"
        );
        alert.getDialogPane().lookup(".content.label").setStyle("-fx-font-size: 15px; -fx-text-fill: white;");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getScene().getRoot().setStyle("-fx-font-family: 'Segoe UI', sans-serif;");
        alert.showAndWait();
    }
}