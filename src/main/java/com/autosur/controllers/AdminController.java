package com.autosur.controllers;

import com.autosur.dao.*;
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

public class AdminController {
    @FXML private TableView<Task> tableTasks;
    @FXML private TableColumn<Task, Integer> colTaskId;
    @FXML private TableColumn<Task, String> colTaskTitle;
    @FXML private TableColumn<Task, String> colTaskDesc;
    @FXML private TableColumn<Task, String> colTaskMech;
    @FXML private TableColumn<Task, String> colTaskStatus;
    @FXML private TextField txtSearchTask;
    @FXML private ComboBox<String> cmbFilterStatus;
    @FXML private TextField txtTaskTitle;
    @FXML private TextArea txtTaskDesc;
    @FXML private ComboBox<User> cmbTaskMech;
    @FXML private ComboBox<Task.Status> cmbTaskStatus;

    @FXML private TableView<User> tableEmployees;
    @FXML private TableColumn<User, Integer> colEmpId;
    @FXML private TableColumn<User, String> colEmpUser;
    @FXML private TextField txtEmpUser;
    @FXML private PasswordField txtEmpPass;

    private final TaskDAO taskDAO = new TaskDAO();
    private final UserDAO userDAO = new UserDAO();
    private Task selectedTask;
    private User selectedEmployee;

    @FXML
    public void initialize() {
        colTaskId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTaskTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTaskDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colTaskMech.setCellValueFactory(new PropertyValueFactory<>("assignedUsername"));
        colTaskStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colEmpId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmpUser.setCellValueFactory(new PropertyValueFactory<>("username"));

        cmbFilterStatus.setItems(FXCollections.observableArrayList("Todos", "Pendiente", "En progreso", "Completada")); [cite: 46]
        cmbFilterStatus.getSelectionModel().selectFirst();
        cmbTaskStatus.setItems(FXCollections.observableArrayList(Task.Status.values()));

        refreshAll();
    }

    private void refreshAll() {
        tableTasks.setItems(FXCollections.observableArrayList(taskDAO.getAllTasks())); [cite: 16, 36]
        tableEmployees.setItems(FXCollections.observableArrayList(userDAO.getAllEmployees()));
        cmbTaskMech.setItems(FXCollections.observableArrayList(userDAO.getAllEmployees())); [cite: 19]
        handleClearTaskForm();
    }

    @FXML
    public void handleFilterTasks() { [cite: 46]
        String filter = cmbFilterStatus.getValue();
        if (filter == null || filter.equals("Todos")) {
            tableTasks.setItems(FXCollections.observableArrayList(taskDAO.getAllTasks())); [cite: 36]
        } else {
            tableTasks.setItems(FXCollections.observableArrayList(taskDAO.getTasksWithFilter(Task.Status.fromString(filter), null))); [cite: 69]
        }
    }

    @FXML
    public void handleSearchTask() { [cite: 47]
        tableTasks.setItems(FXCollections.observableArrayList(taskDAO.searchTasksByName(txtSearchTask.getText().trim(), null))); [cite: 47]
    }

    @FXML
    public void handleSelectTask() { [cite: 45]
        selectedTask = tableTasks.getSelectionModel().getSelectedItem(); [cite: 45]
        if (selectedTask != null) {
            txtTaskTitle.setText(selectedTask.getTitle());
            txtTaskDesc.setText(selectedTask.getDescription());
            cmbTaskStatus.setValue(selectedTask.getStatus());

            for (User u : cmbTaskMech.getItems()) {
                if (u.getId() == selectedTask.getAssignedUserId()) {
                    cmbTaskMech.setValue(u);
                    break;
                }
            }
        }
    }

    @FXML
    public void handleSaveTask() { [cite: 15, 17]
        String title = txtTaskTitle.getText().trim();
        String desc = txtTaskDesc.getText().trim();
        User mech = cmbTaskMech.getValue();
        Task.Status status = cmbTaskStatus.getValue();

        if (title.isEmpty() || mech == null || status == null) {
            showAlert("Error", "Los campos Título, Mecánico y Estado son obligatorios.", Alert.AlertType.ERROR);
            return;
        }

        if (selectedTask == null) {
            Task newTask = new Task(title, desc, mech.getId(), status); [cite: 29]
            taskDAO.createTask(newTask); [cite: 64]
        } else {
            Task updated = new Task(selectedTask.getId(), title, desc, mech.getId(), mech.getUsername(), status);
            taskDAO.updateTaskFull(updated); [cite: 70]
        }
        refreshAll();
    }

    @FXML
    public void handleDeleteTask() { [cite: 18, 43]
        if (selectedTask != null) {
            taskDAO.deleteTask(selectedTask.getId()); [cite: 18, 65]
            refreshAll();
        }
    }

    @FXML
    public void handleClearTaskForm() {
        selectedTask = null;
        txtTaskTitle.clear();
        txtTaskDesc.clear();
        cmbTaskMech.getSelectionModel().clearSelection();
        cmbTaskStatus.setValue(Task.Status.PENDIENTE); [cite: 35]
    }

    @FXML
    public void handleSelectEmployee() {
        selectedEmployee = tableEmployees.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            txtEmpUser.setText(selectedEmployee.getUsername());
        }
    }

    @FXML
    public void handleCreateEmployee() { [cite: 20, 48]
        String user = txtEmpUser.getText().trim();
        String pass = txtEmpPass.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Error", "Usuario y Contraseña requeridos.", Alert.AlertType.ERROR);
            return;
        }

        User newEmp = new User(user, pass, User.Role.EMPLEADO);
        if (userDAO.createUser(newEmp)) { [cite: 20, 66]
            txtEmpUser.clear();
            txtEmpPass.clear();
            refreshAll();
        } else {
            showAlert("Error", "No se pudo crear el usuario (puede que ya exista).", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleDeleteEmployee() { [cite: 21, 48]
        if (selectedEmployee != null) {
            userDAO.deleteUser(selectedEmployee.getId()); [cite: 21, 67]
            selectedEmployee = null;
            txtEmpUser.clear();
            refreshAll();
        }
    }

    @FXML
    public void handleLogout() throws IOException {
        Stage stage = (Stage) tableTasks.getScene().getWindow();
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