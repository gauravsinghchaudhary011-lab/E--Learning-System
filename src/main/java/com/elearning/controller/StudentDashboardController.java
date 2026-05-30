package com.elearning.controller;

import com.elearning.MainApp;
import com.elearning.db.DatabaseManager;
import com.elearning.model.Material;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import com.elearning.UserSession;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;

public class StudentDashboardController implements UiController {
    private TableView<Material> materialsTable;
    private TableColumn<Material, String> titleCol, typeCol;
    private ComboBox<String> subjectFilter;
    private TextField searchField;
    private Button downloadBtn;
    private ListView<String> notificationsList;
    private Button searchBtn, subjectsBtn, contentBtn, logoutBtn;
    private int currentUserId;

    @Override
    public void createAndShowScene() {
        ScrollPane scrollPane = new ScrollPane();
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("Student Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        HBox searchHBox = new HBox();
        searchHBox.getChildren().addAll(
            new Label("Search/Filter: "),
            searchField = new TextField(),
            subjectFilter = new ComboBox<>()
        );
        searchField.setPromptText("Search materials...");
        searchField.setPrefWidth(200);
        subjectFilter.setPrefWidth(100);
        subjectFilter.setPromptText("Subject");

        materialsTable = new TableView<>();
        titleCol = new TableColumn<>("Title");
        typeCol = new TableColumn<>("Type");
        titleCol.setPrefWidth(300);
        typeCol.setPrefWidth(100);
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        materialsTable.getColumns().addAll(titleCol, typeCol);

        HBox downloadHBox = new HBox();
        downloadBtn = new Button("Download Selected");
        downloadHBox.getChildren().add(downloadBtn);

        HBox buttonHBox = new HBox();
        searchBtn = new Button("Advanced Search");
        subjectsBtn = new Button("Subjects");
        contentBtn = new Button("Content");
        logoutBtn = new Button("Logout");
        buttonHBox.getChildren().addAll(searchBtn, subjectsBtn, contentBtn, logoutBtn);

        Separator separator = new Separator();

        Label notificationsLabel = new Label("Notifications");
        notificationsList = new ListView<>();

        vbox.getChildren().addAll(
            titleLabel, searchHBox, materialsTable, downloadHBox, buttonHBox, separator, notificationsLabel, notificationsList
        );

        scrollPane.setContent(vbox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 900, 700);
        MainApp.primaryStage.setScene(scene);
        MainApp.primaryStage.show();

        // Setup logic
        currentUserId = UserSession.getCurrentUserId();
        loadMaterials();
        loadNotifications();
        downloadBtn.setOnAction(e -> downloadSelected());
        searchField.textProperty().addListener((obs, old, newVal) -> filterMaterials());
        searchBtn.setOnAction(e -> MainApp.switchScene(SearchController.class));
        subjectsBtn.setOnAction(e -> MainApp.switchScene(SubjectController.class));
        contentBtn.setOnAction(e -> MainApp.switchScene(ContentController.class));
        logoutBtn.setOnAction(e -> {
            UserSession.logout();
            MainApp.switchScene(LoginController.class);
        });
    }

    private void loadMaterials() {
        try (Connection conn = MainApp.dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT m.*, s.name as subject_name FROM materials m JOIN subjects s ON m.subject_id = s.id")) {
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
            // Populate filter
            ObservableList<String> subjectItems = FXCollections.observableArrayList("All");
            try (Connection conn2 = MainApp.dbManager.getConnection(); PreparedStatement stmt2 = conn2.prepareStatement("SELECT name FROM subjects")) {
                ResultSet rs2 = stmt2.executeQuery();
                while (rs2.next()) {
                    subjectItems.add(rs2.getString("name"));
                }
            } catch (SQLException ex2) {
                ex2.printStackTrace();
            }
            subjectFilter.setItems(subjectItems);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void filterMaterials() {
        String search = searchField.getText().toLowerCase();
        ObservableList<Material> allMaterials = materialsTable.getItems();
        FilteredList<Material> filtered = new FilteredList<>(allMaterials, m -> 
            m.getTitle().toLowerCase().contains(search) || 
            m.getType().toLowerCase().contains(search));
        materialsTable.setItems(filtered);
    }

    private void loadNotifications() {
        try (Connection conn = MainApp.dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT message FROM notifications WHERE user_id = ? ORDER BY date DESC")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            ObservableList<String> notifs = FXCollections.observableArrayList();
            while (rs.next()) {
                notifs.add(rs.getString("message"));
            }
            notificationsList.setItems(notifs);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void downloadSelected() {
        Material selected = materialsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                Path source = Paths.get(selected.getFilePath());
                if (Files.exists(source)) {
                    FileChooser chooser = new FileChooser();
                    String extension = selected.getFilePath().substring(selected.getFilePath().lastIndexOf('.') + 1);
                    chooser.setInitialFileName(selected.getTitle() + "." + extension);
                    File dest = chooser.showSaveDialog(MainApp.primaryStage);
                    if (dest != null) {
                        Files.copy(source, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

