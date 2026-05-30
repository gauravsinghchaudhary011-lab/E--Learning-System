package com.elearning.controller;

import com.elearning.MainApp;
import com.elearning.MainApp;
import com.elearning.db.DatabaseManager;
import com.elearning.model.User;
import com.elearning.controller.StudentDashboardController;
import com.elearning.controller.TeacherDashboardController;
import com.elearning.UserSession;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.sql.*;

public class LoginController implements com.elearning.controller.UiController {
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label errorLabel;

    @Override
    public void createAndShowScene() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(50, 50, 20, 50));

        Label titleLabel = new Label("E-Learning System Login");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(40);
        usernameField.setPrefWidth(300);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);
        passwordField.setPrefWidth(300);

        loginButton = new Button("Login");
        loginButton.setPrefHeight(40);
        loginButton.setPrefWidth(300);
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        loginButton.setOnAction(this::handleLogin);

        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton, errorLabel);

        Scene scene = new Scene(root, 800, 600);
        MainApp.primaryStage.setScene(scene);
        MainApp.primaryStage.show();
    }

    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try (Connection conn = MainApp.dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                String role = rs.getString("role");
                UserSession.setCurrentUser(userId, role);
                if ("teacher".equals(role)) {
                    MainApp.switchScene(TeacherDashboardController.class);
                } else {
                    MainApp.switchScene(StudentDashboardController.class);
                }
                errorLabel.setText("");
            } else {
                errorLabel.setText("Invalid credentials");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            errorLabel.setText("Database error");
        }
    }
}

