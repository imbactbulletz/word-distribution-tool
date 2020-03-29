package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainWindow extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent tabPane = FXMLLoader.load(getClass().getResource("xml/window_main_layout.fxml"));
        primaryStage.setTitle("Word Distribution Tool");
        primaryStage.setScene(new Scene(tabPane, 1280, 720));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}