# FXML to Pure JavaFX Migration TODO

## Steps:
1. [x] Create UiController.java interface
2. [x] Update pom.xml (remove javafx-fxml)
3. [x] Refactor MainApp.java (remove FXMLLoader, new switchScene(Class))
4. [x] Convert LoginController.java to UiController
5. [x] Convert StudentDashboardController.java
6. [x] Convert TeacherDashboardController.java
7. [x] Convert SearchController.java
8. [x] Convert SubjectController.java
9. [x] Convert ContentController.java
10. [x] Delete all .fxml files
11. [x] Test: mvn clean compile javafx:run

Updated: Initial TODO created

