package com.autosur.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:autosur.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL);";

        String createTasksTable = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "user_id INTEGER, " +
                "status TEXT NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE SET NULL);";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createUsersTable);
            stmt.execute(createTasksTable);

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users;");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN');");
                stmt.execute("INSERT INTO users (username, password, role) VALUES ('user', 'user123', 'EMPLOYEE');");
                stmt.execute("INSERT INTO users (username, password, role) VALUES ('juan', 'juan123', 'EMPLOYEE');");

                stmt.execute("INSERT INTO tasks (title, description, user_id, status) VALUES ('Cambio de Aceite', 'Seat Ibiza filtro y aceite', 2, 'Pendiente');");
                stmt.execute("INSERT INTO tasks (title, description, user_id, status) VALUES ('Cambiar Pastillas', 'Frenos delanteros rotos', 3, 'En progreso');");
                System.out.println("Base de datos inicializada con datos de ejemplo correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("Error inicializando la base de datos: " + e.getMessage());
        }
    }
}