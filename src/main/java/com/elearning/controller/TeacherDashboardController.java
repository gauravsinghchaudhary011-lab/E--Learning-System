package com.elearning.controller;

import com.elearning.MainApp;
import com.elearning.db.DatabaseManager;
import com.elearning.model.Subject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ResourceBundle;
import com.elearning.UserSession;

public class TeacherDashboardController implements UiController {
    private TableView<Subject> subjectsTable;
    private TableColumn<Subject, String> nameCol;
    private TextField subjectNameField;
    private Button addSubjectBtn;
    private Button uploadMaterialBtn;
    private TextField materialTitleField;
    private ComboBox<String> subjectCombo;
    private ComboBox<String> typeCombo;
    private Button subjectsBtn, contentBtn, logoutBtn;
    private int currentUserId;

    @Override
    public void createAndShowScene() {
        ScrollPane scrollPane = new ScrollPane();
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("Teacher Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label addSubjectLabel = new Label("Add Subject");
        HBox addSubjectHBox = new HBox();
        subjectNameField = new TextField();
        subjectNameField.setPromptText("Subject Name");
        subjectNameField.setPrefWidth(200);
        addSubjectBtn = new Button("Add");
        addSubjectBtn.setPrefWidth(100);
        addSubjectHBox.getChildren().addAll(subjectNameField, addSubjectBtn);

        subjectsTable = new TableView<>();
        nameCol = new TableColumn<>("Subjects");
        nameCol.setPrefWidth(400);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        subjectsTable.getColumns().add(nameCol);

        Separator separator1 = new Separator();

        Label uploadLabel = new Label("Upload Material");
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        Label subjectLabel = new Label("Subject:");
        subjectCombo = new ComboBox<>();
        subjectCombo.setPrefWidth(150);
        Label titleLabel2 = new Label("Title:");
        materialTitleField = new TextField();
        materialTitleField.setPromptText("Material Title");
        materialTitleField.setPrefWidth(150);
        Label typeLabel = new Label("Type:");
        typeCombo = new ComboBox<>();
        typeCombo.setPrefWidth(150);
        uploadMaterialBtn = new Button("Upload");

        gridPane.add(subjectLabel, 0, 0);
        gridPane.add(subjectCombo, 1, 0);
        gridPane.add(titleLabel2, 0, 1);
        gridPane.add(materialTitleField, 1, 1);
        gridPane.add(typeLabel, 0, 2);
        gridPane.add(typeCombo, 1, 2);
        gridPane.add(uploadMaterialBtn, 1, 3);

        Separator separator2 = new Separator();

        HBox bottomHBox = new HBox();
        subjectsBtn = new Button("Manage Subjects");
        contentBtn = new Button("View Content");
        logoutBtn = new Button("Logout");
        bottomHBox.getChildren().addAll(subjectsBtn, contentBtn, logoutBtn);

        vbox.getChildren().addAll(titleLabel, addSubjectLabel, addSubjectHBox, subjectsTable, separator1, uploadLabel, gridPane, separator2, bottomHBox);

        scrollPane.setContent(vbox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 900, 700);
        MainApp.primaryStage.setScene(scene);
        MainApp.primaryStage.show();

        // Setup logic
        currentUserId = UserSession.getCurrentUserId();
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        loadSubjects();
        typeCombo.getItems().addAll("notes", "pyq", "questions");
        addSubjectBtn.setOnAction(e -> addSubject());
        uploadMaterialBtn.setOnAction(e -> uploadMaterial());
        subjectsBtn.setOnAction(e -> MainApp.switchScene(SubjectController.class));
        contentBtn.setOnAction(e -> MainApp.switchScene(ContentController.class));
        logoutBtn.setOnAction(e -> {
            UserSession.logout();
            MainApp.switchScene(LoginController.class);
        });
    }

    private void loadSubjects() {
        try (Connection conn = MainApp.dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM subjects WHERE teacher_id = ?")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            ObservableList<Subject> subjects = FXCollections.observableArrayList();
            while (rs.next()) {
                subjects.add(new Subject(rs.getInt("id"), rs.getString("name"), rs.getInt("teacher_id")));
            }
            subjectsTable.setItems(subjects);
            subjectCombo.setItems(FXCollections.observableArrayList("All"));
            subjects.forEach(s -> subjectCombo.getItems().add(s.getName()));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addSubject() {
        String name = subjectNameField.getText();
        if (!name.isEmpty()) {
            try (Connection conn = MainApp.dbManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO subjects (name, teacher_id) VALUES (?, ?)")) {
                stmt.setString(1, name);
                stmt.setInt(2, currentUserId);
                stmt.executeUpdate();
                loadSubjects();
                subjectNameField.clear();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void uploadMaterial() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Material File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF & Docs", "*.pdf", "*.docx"));
        File file = fileChooser.showOpenDialog(MainApp.primaryStage);

        if (file != null) {
            String title = materialTitleField.getText();
            String subjectName = subjectCombo.getValue();
            String type = typeCombo.getValue();
            if (title != null && !title.isEmpty() && subjectName != null && type != null) {
                int subjectId = getSubjectIdByName(subjectName);
                if (subjectId > 0) {
                    Path dest = Paths.get("data/files/" + file.getName());
                    try {
                        Files.createDirectories(dest.getParent());
                        Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } 

                    try (Connection conn = MainApp.dbManager.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(
                                 "INSERT INTO materials (subject_id, title, type, file_path) VALUES (?, ?, ?, ?)")) {
                        stmt.setInt(1, subjectId);
                        stmt.setString(2, title);
                        stmt.setString(3, type);
                        stmt.setString(4, dest.toString());
                        stmt.executeUpdate();

                        // Notify all students
                        PreparedStatement studentStmt = conn.prepareStatement("SELECT id FROM users WHERE role = 'student'");
                        ResultSet studentRs = studentStmt.executeQuery();
                        PreparedStatement notifStmt = conn.prepareStatement("INSERT INTO notifications (user_id, message) VALUES (?, ?)");
                        while (studentRs.next()) {
                            notifStmt.setInt(1, studentRs.getInt("id"));
                            notifStmt.setString(2, "New material uploaded: " + title);
                            notifStmt.addBatch();
                        }
                        notifStmt.executeBatch();
                        studentRs.close();
                        studentStmt.close();

                        materialTitleField.clear();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private int getSubjectIdByName(String name) {
        try (Connection conn = MainApp.dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM subjects WHERE name = ? AND teacher_id = ?")) {
            stmt.setString(1, name);
            stmt.setInt(2, currentUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }
}

