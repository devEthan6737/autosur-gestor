package com.autosur.controllers;

import com.autosur.dao.TaskDAO;
import com.autosur.models.Task;
import com.autosur.models.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;

public class EmployeeController {

    @FXML private Label lblWelcome;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterStatus;
    @FXML private ComboBox<Task.Status> cmbChangeStatus;
    @FXML private TableView<Task> tableTasks;
    @FXML private TableColumn<Task, Integer> colId;
    @FXML private TableColumn<Task, String> colTitle;
    @FXML private TableColumn<Task, String> colDescription;
    @FXML private TableColumn<Task, String> colStatus;

    private final TaskDAO taskDAO = new TaskDAO();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = LoginController.getLoggedUser();
        if (currentUser != null) {
            lblWelcome.setText("Mecánico: " + currentUser.getUsername());
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        cmbFilterStatus.setItems(FXCollections.observableArrayList("Todos", "Pendiente", "En progreso", "Completada")); [cite: 26, 46]
        cmbFilterStatus.getSelectionModel().selectFirst();
        
        cmbChangeStatus.setItems(FXCollections.observableArrayList(Task.Status.values())); [cite: 37]

        loadTasks();
    }

    private void loadTasks() {
        if (currentUser != null) {
            tableTasks.setItems(FXCollections.observableArrayList(taskDAO.getTasksByEmployee(currentUser.getId()))); [cite: 24]
        }
    }

    @FXML
    public void handleFilter() { [cite: 26, 46]
        String filter = cmbFilterStatus.getValue();
        if (filter == null || filter.equals("Todos")) {
            loadTasks();
        } else {
            Task.Status status = Task.Status.fromString(filter);
            tableTasks.setItems(FXCollections.observableArrayList(taskDAO.getTasksWithFilter(status, currentUser.getId()))); [cite: 26]
        }
    }

    @FXML
    public void handleSearch() { [cite: 47]
        String query = txtSearch.getText().trim();
        tableTasks.setItems(FXCollections.observableArrayList(taskDAO.searchTasksByName(query, currentUser.getId()))); [cite: 47]
    }

    @FXML
    public void handleUpdateStatus() { [cite: 25, 37]
        Task selectedTask = tableTasks.getSelectionModel().getSelectedItem(); [cite: 45]
        Task.Status newStatus = cmbChangeStatus.getValue();

        if (selectedTask == null || newStatus == null) {
            showAlert("Atención", "Selecciona una tarea de la lista y el nuevo estado.", Alert.AlertType.WARNING);
            return;
        }

        if (taskDAO.updateTaskStatus(selectedTask.getId(), newStatus)) { [cite: 25, 37]
            loadTasks();
            showAlert("Éxito", "Estado actualizado correctamente.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void handleLogout() throws IOException {
        Stage stage = (Stage) lblWelcome.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("AutoSur - Control de Acceso");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}