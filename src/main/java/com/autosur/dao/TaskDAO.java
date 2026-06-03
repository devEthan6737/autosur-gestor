package com.autosur.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.autosur.database.DatabaseConnection;
import com.autosur.models.Task;

public class TaskDAO {

    public List<Task> getAllTasks() {
        return getTasksWithFilter(null, null);
    }

    public List<Task> getTasksByEmployee(int userId) {
        return getTasksWithFilter(null, userId);
    }

    public List<Task> getTasksWithFilter(Task.Status status, Integer userId) {
        List<Task> tasks = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT t.*, u.username FROM tasks t LEFT JOIN users u ON t.user_id = u.id WHERE 1=1"
        );

        if (status != null) query.append(" AND t.status = ?");
        if (userId != null) query.append(" AND t.user_id = ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {
            
            int paramIndex = 1;
            if (status != null) ps.setString(paramIndex++, status.getValue());
            if (userId != null) ps.setInt(paramIndex, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        Task.Status.fromString(rs.getString("status"))
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al filtrar tareas: " + e.getMessage());
        }
        return tasks;
    }

    public List<Task> searchTasksByName(String name, Integer userId) {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT t.*, u.username FROM tasks t LEFT JOIN users u ON t.user_id = u.id " +
                       "WHERE t.title LIKE ?" + (userId != null ? " AND t.user_id = ?" : "");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, "%" + name + "%");
            if (userId != null) ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        Task.Status.fromString(rs.getString("status"))
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en búsqueda por nombre: " + e.getMessage());
        }
        return tasks;
    }

    public boolean createTask(Task task) {
        String query = "INSERT INTO tasks (title, description, user_id, status) VALUES (?, ?, ?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setInt(3, task.getAssignedUserId());
            ps.setString(4, task.getStatus().getValue());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear tarea: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTaskStatus(int taskId, Task.Status newStatus) {
        String query = "UPDATE tasks SET status = ? WHERE id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, newStatus.getValue());
            ps.setInt(2, taskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTaskFull(Task task) {
        String query = "UPDATE tasks SET title = ?, description = ?, user_id = ?, status = ? WHERE id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setInt(3, task.getAssignedUserId());
            ps.setString(4, task.getStatus().getValue());
            ps.setInt(5, task.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al modificar tarea completa: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTask(int taskId) {
        String query = "DELETE FROM tasks WHERE id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, taskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar tarea: " + e.getMessage());
            return false;
        }
    }
}