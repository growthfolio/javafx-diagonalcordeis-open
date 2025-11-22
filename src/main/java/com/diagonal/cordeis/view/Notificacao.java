package com.diagonal.cordeis.view;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Notificacao {

    public static void sucesso(String mensagem) {
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
        exibirToast(icon, mensagem, "#43a047"); // verde
    }

    public static void erro(String mensagem) {
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.TIMES_CIRCLE);
        exibirToast(icon, mensagem, "#e53935"); // vermelho
    }

    public static void informacao(String mensagem) {
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.INFO_CIRCLE);
        exibirToast(icon, mensagem, "#1e88e5"); // azul
    }

    public static void alerta(String mensagem) {
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
        exibirToast(icon, mensagem, "#ff9800"); // laranja
    }

    private static void exibirToast(Node icone, String mensagem, String corFundo) {
        Platform.runLater(() -> {
            if (icone instanceof FontAwesomeIconView) {
                FontAwesomeIconView icon = (FontAwesomeIconView) icone;
                icon.setSize("20");
                icon.setFill(Color.WHITE);
                icon.setStyle("-fx-padding: 0 10 0 0;");
            }

            Label textLabel = new Label(mensagem);
            textLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: white;");

            HBox content = new HBox(icone, textLabel);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setSpacing(8);
            content.setStyle("-fx-padding: 14px 24px; -fx-background-radius: 12px; -fx-background-color: " + corFundo + ";");

            content.setEffect(new DropShadow(18, Color.rgb(0,0,0,0.18)));

            StackPane pane = new StackPane(content);
            pane.setStyle("-fx-padding: 10px;");
            Popup popup = new Popup();
            popup.getContent().add(pane);
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);

            Stage stage = getMainStage();
            if (stage != null) {
                popup.show(stage);
                FadeTransition fade = new FadeTransition(Duration.seconds(3), pane);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setOnFinished(e -> popup.hide());
                fade.play();
            }
        });
    }

    private static Stage getMainStage() {
        for (Stage stage : Stage.getWindows().stream().filter(w -> w instanceof Stage).map(w -> (Stage) w).toList()) {
            if (stage.isShowing()) return stage;
        }
        return null;
    }
}