package com.elearning;

import com.elearning.controller.LoginController;
import com.elearning.controller.UiController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.elearning.db.DatabaseManager;

public class MainApp extends Application {
    public static Stage primaryStage;
    public static DatabaseManager dbManager = new DatabaseManager();

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("E-Learning System");

        new LoginController().createAndShowScene();
    }

    public static <T extends UiController> void switchScene(Class<T> controllerClass) {
        try {
            T controller = controllerClass.getDeclaredConstructor().newInstance();
            controller.createAndShowScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

