package com.agendamento.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

public class AgendamentoApp extends Application {

    // Remove as referências ao Spring que estão causando erro
    // private ConfigurableApplicationContext springContext;

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("🖥️ Iniciando interface JavaFX...");

        // Carrega a tela de login
        URL loginFxml = getClass().getResource("/views/Login.fxml");
        if (loginFxml == null) {
            throw new RuntimeException("❌ Arquivo Login.fxml não encontrado!");
        }

        FXMLLoader loader = new FXMLLoader(loginFxml);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        // Aplica CSS se existir
        try {
            URL cssUrl = getClass().getResource("/styles/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.out.println("⚠️ CSS não encontrado, continuando sem estilo...");
        }

        // Configura a janela
        primaryStage.setTitle("MediSchedule - Sistema de Agendamento Médico");

        // Tenta carregar ícone
        try {
            Image icon = new Image(getClass().getResourceAsStream("/icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("ℹ️ Ícone não encontrado");
        }

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        System.out.println("✅ Interface JavaFX carregada com sucesso!");
    }

    // Método opcional para receber contexto Spring
    public void setSpringContext(Object context) {
        // Pode ser implementado se necessário
        System.out.println("Contexto Spring recebido (opcional)");
    }
}