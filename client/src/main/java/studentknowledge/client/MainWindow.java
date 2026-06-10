package studentknowledge.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import studentknowledge.view.LoginView;

public class MainWindow extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("学生个人知识库与日程标注系统");
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);

        showLogin();
        stage.show();
    }

    public static void showLogin() {
        LoginView loginView = new LoginView();
        primaryStage.setScene(new Scene(loginView.getRoot()));
    }

    public static void showMain() {
        studentknowledge.view.MainLayout mainLayout = new studentknowledge.view.MainLayout();
        primaryStage.setScene(new Scene(mainLayout.getRoot()));
    }

    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) {
        launch(args);
    }
}
