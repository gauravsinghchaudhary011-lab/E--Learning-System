package com.elearning.db;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:h2:./elearning";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";
    private Connection connection;

    public DatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(20) NOT NULL CHECK (role IN ('teacher', 'student'))
                )
                """;
            String subjectsTable = """
                CREATE TABLE IF NOT EXISTS subjects (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    teacher_id INT,
                    FOREIGN KEY (teacher_id) REFERENCES users(id)
                )
                """;
            String materialsTable = """
                CREATE TABLE IF NOT EXISTS materials (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    subject_id INT,
                    title VARCHAR(200) NOT NULL,
                    type VARCHAR(50) NOT NULL,
                    file_path VARCHAR(500),
                    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (subject_id) REFERENCES subjects(id)
                )
                """;
            String notificationsTable = """
                CREATE TABLE IF NOT EXISTS notifications (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT,
                    message TEXT,
                    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """;

            stmt.execute(usersTable);
            stmt.execute(subjectsTable);
            stmt.execute(materialsTable);
            stmt.execute(notificationsTable);

            // Insert default users for testing
            String insertDefault = "INSERT INTO users (username, password, role) SELECT 'teacher1', 'teacherpass', 'teacher' WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'teacher1'); INSERT INTO users (username, password, role) SELECT 'student1', 'studentpass', 'student' WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'student1')";
            stmt.execute(insertDefault);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
