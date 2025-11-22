package com.diagonal.cordeis;

import com.diagonal.cordeis.security.AuthenticationService;
import com.diagonal.cordeis.view.SpringContext;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class MainView extends Application {

    private ConfigurableApplicationContext springContext;
    private AuthenticationService authService;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(DiagonalCordeisApplication.class).run();
        SpringContext.setContext(springContext);
        authService = springContext.getBean(AuthenticationService.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        authService.setPrimaryStage(primaryStage);
        primaryStage.setTitle("Sistema de Gerenciamento de Cordéis");

        // Sempre iniciar com a tela principal, permitindo acesso à impressão sem autenticação
        // A interface será adaptada com base nas permissões do usuário (ou falta delas)
        authService.loadMainView();
    }

    @Override
    public void stop() {
        springContext.close();
    }
}
