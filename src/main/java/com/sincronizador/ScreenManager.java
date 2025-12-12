package com.sincronizador;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ScreenManager {

    private static Stage stage;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void changeScreen(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(ScreenManager.class.getResource("/view/" + fxmlFile));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            System.err.println("Erro ao carregar tela: " + fxmlFile);
            e.printStackTrace();
        }
    }
}
