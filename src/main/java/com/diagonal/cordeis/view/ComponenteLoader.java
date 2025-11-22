package com.diagonal.cordeis.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;

@Slf4j
public class ComponenteLoader {

    @Getter
    private final Object controller;

    private final Parent root;

    public ComponenteLoader(String caminhoFxml) {
        try {
            URL resource = getClass().getResource(caminhoFxml);
            if (resource == null) {
                throw new IllegalStateException("FXML não encontrado: " + caminhoFxml);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            loader.setControllerFactory(SpringContext.getContext()::getBean);
            this.root = loader.load();
            this.controller = loader.getController();

        } catch (Exception e) {
            log.error("Erro ao carregar FXML '{}': {}", caminhoFxml, e.getMessage(), e);
            throw new RuntimeException("Erro ao carregar a interface: " + caminhoFxml, e);
        }
    }


    public void showModal(String titulo) {
        Stage modalStage = ModalUtils.createModalStage(titulo, root, null);
        modalStage.showAndWait();
    }
}
