package com.elearning.controller;

import com.elearning.MainApp;
import com.elearning.UserSession;
import com.elearning.model.Material;
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
import java.time.format.DateTimeFormatter;

public class ContentController implements UiController {
    private TableView<Material> materialsTable;
    private TableColumn<Material, String> titleCol, typeCol, dateCol;
    private ComboBox<String> subjectCombo;
    private Button loadBtn, backBtn;

    @Override
    public void createAndShowScene() {
        ScrollPane scrollPane = new ScrollPane();
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("Content Management");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        HBox topHBox = new HBox();
        subjectCombo = new ComboBox<>();
        subjectCombo.setPromptText("Select Subject");
        subjectCombo.setPrefWidth(200);
        loadBtn = new Button("Load Content");
        topHBox.getChildren().addAll(subjectCombo, loadBtn);

        materialsTable = new TableView<>();
        titleCol = new TableColumn<>("Title");
        typeCol = new TableColumn<>("Type");
        dateCol = new TableColumn<>("Upload Date");
        titleCol.setPrefWidth(300);
        typeCol.setPrefWidth(100);
        dateCol.setPrefWidth(200);
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUploadDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getUploadDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new SimpleStringProperty("");
        });
        materialsTable.getColumns().addAll(titleCol, typeCol, dateCol);

        HBox bottomHBox = new HBox();
        backBtn = new Button("Back");
        bottomHBox.getChildren().add(backBtn);

        vbox.getChildren().addAll(titleLabel, topHBox, materialsTable, bottomHBox);

        scrollPane.setContent(vbox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 900, 700);
        MainApp.primaryStage.setScene(scene);
        MainApp.primaryStage.show();

        // Setup
        loadBtn.setOnAction(e -> loadContent());
        backBtn.setOnAction(e -> {
            if ("teacher".equals(UserSession.getCurrentRole())) {
                MainApp.switchScene(TeacherDashboardController.class);
            } else {
                MainApp.switchScene(StudentDashboardController.class);
            }
        });
        // Populate subjects
        populateSubjects();
    }

    private void populateSubjects() {
        try (Connection conn = MainApp.dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM subjects")) {
            ResultSet rs = stmt.executeQuery();
            ObservableList<String> subjects = FXCollections.observableArrayList("All");
            while (rs.next()) {
                subjects.add(rs.getString("name"));
            }
            subjectCombo.setItems(subjects);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadContent() {
        String subjectName = subjectCombo.getValue();
        if (subjectName != null && !subjectName.equals("All")) {
            try (Connection conn = MainApp.dbManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT m.*, s.name as subject_name FROM materials m JOIN subjects s ON m.subject_id = s.id WHERE s.name = ?")) {
                stmt.setString(1, subjectName);
                ResultSet rs = stmt.executeQuery();
                ObservableList<Material> materials = FXCollections.observableArrayList();
                while (rs.next()) {
                    materials.add(new Material(
                        rs.getInt("id"),
                        rs.getInt("subject_id"),
                        rs.getString("title"),
                        rs.getString("type"),
                        rs.getString("file_path"),
                        rs.getTimestamp("upload_date").toLocalDateTime()
                    ));
                }
                materialsTable.setItems(materials);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            loadAllContent();
        }
    }

    private void loadAllContent() {
        try (Connection conn = MainApp.dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT m.*, s.name as subject_name FROM materials m JOIN subjects s ON m.subject_id = s.id")) {
            ResultSet rs = stmt.executeQuery();
            ObservableList<Material> materials = FXCollections.observableArrayList();
            while (rs.next()) {
                materials.add(new Material(
                    rs.getInt("id"),
                    rs.getInt("subject_id"),
                    rs.getString("title"),
                    rs.getString("type"),
                    rs.getString("file_path"),
                    rs.getTimestamp("upload_date").toLocalDateTime()
                ));
            }
            materialsTable.setItems(materials);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

