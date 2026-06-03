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
import java.util.ArrayList;
import java.util.List;

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
        // Columnas de Tareas
        colTaskId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTaskTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTaskDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colTaskMech.setCellValueFactory(new PropertyValueFactory<>("assignedUsername"));
        colTaskStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Columnas de Empleados
        colEmpId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmpUser.setCellValueFactory(new PropertyValueFactory<>("username"));

        // Combos de Estado
        cmbFilterStatus.setItems(FXCollections.observableArrayList("Todos", "Pendiente", "En progreso", "Completada"));
        cmbFilterStatus.getSelectionModel().selectFirst();
        cmbTaskStatus.setItems(FXCollections.observableArrayList(Task.Status.values()));

        // Formateador visual de los mecánicos para que no salga el puntero de memoria en blanco
        cmbTaskMech.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getUsername());
                    setStyle("-fx-text-fill: white;"); // Fuerza el color de la letra
                }
            }
        });
        cmbTaskMech.setButtonCell(cmbTaskMech.getCellFactory().call(null));

        refreshAll();
    }

private void refreshAll() {
    List<User> employees = userDAO.getAllEmployees();
    if (employees == null) {
        employees = new ArrayList<>();
    }

    System.out.println("\n[AutoSur-DATABASE-DEBUG] -> Empleados devueltos por la Query del DAO: " + employees.size());

    if (employees.isEmpty()) {
        System.out.println("[AutoSur-DATABASE-DEBUG] -> Alerta: El DAO no encuentra empleados en el archivo .db.");

    }
    // Cargar tablas y combos con lo que realmente devuelve el DAO
    tableTasks.setItems(FXCollections.observableArrayList(taskDAO.getAllTasks()));
    tableEmployees.setItems(FXCollections.observableArrayList(employees));
    cmbTaskMech.setItems(FXCollections.observableArrayList(employees));

    selectedTask = null;
    txtTaskTitle.clear();
    txtTaskDesc.clear();
    cmbTaskMech.setValue(null);
    cmbTaskStatus.setValue(Task.Status.Pending);
    tableTasks.getSelectionModel().clearSelection();

    selectedEmployee = null;
    txtEmpUser.clear();
    txtEmpPass.clear();
    tableEmployees.getSelectionModel().clearSelection();
}

    @FXML
    public void handleFilterTasks() {
        String filter = cmbFilterStatus.getValue();
        if (filter == null || filter.equals("Todos")) {
            tableTasks.setItems(FXCollections.observableArrayList(taskDAO.getAllTasks()));
        } else {
            tableTasks.setItems(FXCollections.observableArrayList(taskDAO.getTasksWithFilter(Task.Status.fromString(filter), null)));
        }
    }

    @FXML
    public void handleSearchTask() {
        tableTasks.setItems(FXCollections.observableArrayList(taskDAO.searchTasksByName(txtSearchTask.getText().trim(), null)));
    }

    @FXML
    public void handleSelectTask() {
        selectedTask = tableTasks.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            txtTaskTitle.setText(selectedTask.getTitle());
            txtTaskDesc.setText(selectedTask.getDescription());
            cmbTaskStatus.setValue(selectedTask.getStatus());

            User matchedMechanic = cmbTaskMech.getItems().stream()
                .filter(user -> user.getId() == selectedTask.getAssignedUserId())
                .findFirst()
                .orElse(null);

            if (matchedMechanic != null) {
                cmbTaskMech.setValue(matchedMechanic);
            } else {
                cmbTaskMech.setValue(null);
            }
        }
    }

    @FXML
    public void handleSaveTask() {
        String title = txtTaskTitle.getText().trim();
        String desc = txtTaskDesc.getText().trim();
        User mech = cmbTaskMech.getValue();
        Task.Status status = cmbTaskStatus.getValue();

        if (title.isEmpty() || mech == null || status == null) {
            showAlert("Error", "Los campos Título, Mecánico y Estado son obligatorios.", Alert.AlertType.ERROR);
            return;
        }

        if (selectedTask == null) {
            Task newTask = new Task(title, desc, mech.getId(), status);
            taskDAO.createTask(newTask);
        } else {
            Task updated = new Task(selectedTask.getId(), title, desc, mech.getId(), mech.getUsername(), status);
            taskDAO.updateTaskFull(updated);
        }
        refreshAll();
    }

    @FXML
    public void handleDeleteTask() {
        if (selectedTask != null) {
            taskDAO.deleteTask(selectedTask.getId());
            refreshAll();
        }
    }

    @FXML
    public void handleClearTaskForm() {
        refreshAll();
    }

    @FXML
    public void handleSelectEmployee() {
        selectedEmployee = tableEmployees.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            txtEmpUser.setText(selectedEmployee.getUsername());
            txtEmpPass.clear();
        }
    }

    @FXML
    public void handleCreateEmployee() {
        String user = txtEmpUser.getText().trim();
        String pass = txtEmpPass.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Error", "Usuario y Contraseña requeridos.", Alert.AlertType.ERROR);
            return;
        }

        User newEmp = new User(user, pass, User.Role.EMPLOYEE);
        if (userDAO.createUser(newEmp)) {
            System.out.println("[AutoSur-DEBUG] -> Empleado '" + user + "' creado con éxito en BD.");
            refreshAll(); // Ahora refrescará la lista real incluyendo al nuevo usuario
        } else {
            showAlert("Error", "No se pudo crear el usuario (puede que ya exista).", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleDeleteEmployee() {
        if (selectedEmployee != null) {
            userDAO.deleteUser(selectedEmployee.getId());
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