package com.autosur.controllers;

import com.autosur.dao.UserDAO;
import com.autosur.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final UserDAO userDAO = new UserDAO();
    
    private static User loggedUser;
    public static User getLoggedUser() { return loggedUser; }

    @FXML
    public void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Por favor, rellena todos los campos.");
            return;
        }

        User user = userDAO.login(username, password);

        if (user != null) {
            loggedUser = user;
            navigateToDashboard(user.getRole());
        } else {
            lblError.setText("Usuario o contraseña incorrectos.");
        }
    }

    private void navigateToDashboard(User.Role role) {
        String fxmlFile = (role == User.Role.ADMIN) ? "/views/admin.fxml" : "/views/employee.fxml";
        String windowTitle = (role == User.Role.ADMIN) ? "AutoSur - Panel de Administración" : "AutoSur - Panel de Mecánico";

        try {
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            if (getClass().getResource("/css/styles.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            }
            
            stage.setTitle(windowTitle);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            lblError.setText("Error al cargar la interfaz.");
            e.printStackTrace();
        }
    }
}