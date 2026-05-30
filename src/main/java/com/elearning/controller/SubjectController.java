package com.elearning.controller;

import com.elearning.MainApp;
import com.elearning.UserSession;
import com.elearning.model.Subject;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SubjectController implements UiController {
    private TableView<Subject> subjectsTable;
    private TableColumn<Subject, String> nameCol, teacherCol;
    private Button refreshBtn, backBtn;

    @Override
    public void createAndShowScene() {
        ScrollPane scrollPane = new ScrollPane();
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("Subjects Management");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        subjectsTable = new TableView<>();
        nameCol = new TableColumn<>("Name");
        teacherCol = new TableColumn<>("Teacher");
        nameCol.setPrefWidth(400);
        teacherCol.setPrefWidth(200);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        teacherCol.setCellValueFactory(cellData -> new SimpleStringProperty(getTeacherName(cellData.getValue().getTeacherId())));
        subjectsTable.getColumns().addAll(nameCol, teacherCol);

        HBox buttonHBox = new HBox();
        refreshBtn = new Button("Refresh");
        backBtn = new Button("Back to Dashboard");
        buttonHBox.getChildren().addAll(refreshBtn, backBtn);

        vbox.getChildren().addAll(titleLabel, subjectsTable, buttonHBox);

        scrollPane.setContent(vbox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 900, 700);
        MainApp.primaryStage.setScene(scene);
        MainApp.primaryStage.show();

        // Setup
        refreshBtn.setOnAction(e -> loadSubjects());
        backBtn.setOnAction(e -> {
            if ("teacher".equals(UserSession.getCurrentRole())) {
                MainApp.switchScene(TeacherDashboardController.class);
            } else {
                MainApp.switchScene(StudentDashboardController.class);
            }
        });
        loadSubjects();
    }

    private void loadSubjects() {
        try (Connection conn = MainApp.dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM subjects")) {
            ResultSet rs = stmt.executeQuery();
            ObservableList<Subject> subjects = FXCollections.observableArrayList();
            while (rs.next()) {
                subjects.add(new Subject(rs.getInt("id"), rs.getString("name"), rs.getInt("teacher_id")));
            }
            subjectsTable.setItems(subjects);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String getTeacherName(int teacherId) {
        try (Connection conn = MainApp.dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE id = ?")) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "Unknown";
    }
}

