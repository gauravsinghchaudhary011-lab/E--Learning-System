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
import java.time.LocalDateTime;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class SearchController implements UiController {
    private TableView<Material> resultsTable;
    private TableColumn<Material, String> titleCol, subjectCol, typeCol, dateCol;
    private ComboBox<String> subjectCombo, typeCombo;
    private TextField keywordField;
    private Button searchBtn, backBtn;

    @Override
    public void createAndShowScene() {
        ScrollPane scrollPane = new ScrollPane();
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("Advanced Search");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        GridPane gridPane = new GridPane(10, 10);
        Label subjectLabel = new Label("Subject:");
        subjectCombo = new ComboBox<>();
        subjectCombo.setPromptText("Subject");
        subjectCombo.setPrefWidth(150);
        Label typeLabel = new Label("Type:");
        typeCombo = new ComboBox<>();
        typeCombo.setPromptText("Type");
        typeCombo.setPrefWidth(150);
        Label keywordLabel = new Label("Keyword:");
        keywordField = new TextField();
        keywordField.setPromptText("Search keyword");
        keywordField.setPrefWidth(150);
        searchBtn = new Button("Search");

        gridPane.add(subjectLabel, 0, 0);
        gridPane.add(subjectCombo, 1, 0);
        gridPane.add(typeLabel, 0, 1);
        gridPane.add(typeCombo, 1, 1);
        gridPane.add(keywordLabel, 0, 2);
        gridPane.add(keywordField, 1, 2);
        gridPane.add(searchBtn, 1, 3);

        resultsTable = new TableView<>();
        titleCol = new TableColumn<>("Title");
        subjectCol = new TableColumn<>("Subject");
        typeCol = new TableColumn<>("Type");
        dateCol = new TableColumn<>("Date");
        titleCol.setPrefWidth(300);
        subjectCol.setPrefWidth(150);
        typeCol.setPrefWidth(100);
        dateCol.setPrefWidth(150);
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        subjectCol.setCellValueFactory(cellData -> new SimpleStringProperty("Subject"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUploadDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getUploadDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            return new SimpleStringProperty("");
        });
        resultsTable.getColumns().addAll(titleCol, subjectCol, typeCol, dateCol);

        backBtn = new Button("Back to Dashboard");

        vbox.getChildren().addAll(titleLabel, gridPane, resultsTable, backBtn);

        scrollPane.setContent(vbox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 900, 700);
        MainApp.primaryStage.setScene(scene);
        MainApp.primaryStage.show();

        // Setup
        typeCombo.getItems().addAll("notes", "pyq", "questions", "All");
        typeCombo.setValue("All");
        searchBtn.setOnAction(e -> performSearch());
        backBtn.setOnAction(e -> MainApp.switchScene(StudentDashboardController.class));
    }

    private void performSearch() {
        String subject = subjectCombo.getValue();
        String type = typeCombo.getValue();
        String keyword = keywordField.getText().toLowerCase();

        StringBuilder sql = new StringBuilder("SELECT m.*, s.name as subject_name FROM materials m JOIN subjects s ON m.subject_id = s.id WHERE 1=1");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (subject != null && !subject.isEmpty() && !subject.equals("All")) {
            sql.append(" AND s.name = ?");
            params.add(subject);
        }
        if (type != null && !type.equals("All")) {
            sql.append(" AND m.type = ?");
            params.add(type);
        }
        if (!keyword.isEmpty()) {
            sql.append(" AND LOWER(m.title) LIKE ?");
            params.add("%" + keyword + "%");
        }

        try (Connection conn = MainApp.dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            ObservableList<Material> results = FXCollections.observableArrayList();
            while (rs.next()) {
                results.add(new Material(
                    rs.getInt("id"),
                    rs.getInt("subject_id"),
                    rs.getString("title"),
                    rs.getString("type"),
                    rs.getString("file_path"),
                    rs.getTimestamp("upload_date").toLocalDateTime()
                ));
            }
            resultsTable.setItems(results);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

