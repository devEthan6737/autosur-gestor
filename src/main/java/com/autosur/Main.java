package com.autosur;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.autosur.database.DatabaseConnection;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseConnection.initDatabase();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            
            if (getClass().getResource("/css/styles.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            }

            primaryStage.setTitle("AutoSur - Gestión de Taller");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar la interfaz de login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}