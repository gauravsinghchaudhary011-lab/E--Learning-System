@echo off
set FX_PATH=openjfx-sdk/javafx-sdk-17.0.12/lib
java --module-path "%FX_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.web -cp "target/classes;./h2-2.2.224.jar" com.elearning.MainApp
pause
