package com.diagonal.cordeis.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;

@Slf4j
@Component
@Getter
@Setter
@RequiredArgsConstructor
public class FXMLViewLoader {
    private final ApplicationContext applicationContext;

    public Parent load(String fxmlPath) {
        try {
            FXMLLoader loader = createLoader(fxmlPath);
            return loader.load();
        } catch (IOException e) {
            log.error("Erro ao carregar FXML '{}': {}", fxmlPath, e.getMessage(), e);
            throw new RuntimeException("Erro ao carregar a interface: " + fxmlPath, e);
        }
    }

    public Scene loadScene(String fxmlPath) {
        return new Scene(load(fxmlPath));
    }

    public <T> FXMLView<T> loadWithController(String fxmlPath) {
        try {
            FXMLLoader loader = createLoader(fxmlPath);
            Parent root = loader.load();
            T controller = loader.getController();
            return new FXMLView<>(root, controller);
        } catch (IOException e) {
            log.error("Erro ao carregar FXML '{}': {}", fxmlPath, e.getMessage(), e);
            throw new RuntimeException("Erro ao carregar a interface: " + fxmlPath, e);
        }
    }

    private FXMLLoader createLoader(String fxmlPath) {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new IllegalStateException("FXML não encontrado: " + fxmlPath);
        }
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(applicationContext::getBean);
        return loader;
    }

    public record FXMLView<T>(Parent root, T controller) {}
}
