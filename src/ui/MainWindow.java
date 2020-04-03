package ui;

import com.sun.tools.javac.Main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ui.controller.MainController;

public class MainWindow extends Application {

    public static final MainController MAIN_CONTROLLER = new MainController();

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("xml/window_main_layout.fxml"));
        fxmlLoader.setController(MAIN_CONTROLLER);
        GridPane gridPane = fxmlLoader.load();

        fxmlLoader = new FXMLLoader(getClass().getResource("xml/pane_input_layout.fxml"));
        fxmlLoader.setController(MainController.INPUT_CONTROLLER);
        Parent inputPane = fxmlLoader.load();
        MainController.INPUT_CONTROLLER.init();
        gridPane.add(inputPane, 0, 0, 1, 3);

        fxmlLoader = new FXMLLoader(getClass().getResource("xml/pane_cruncher_layout.fxml"));
        fxmlLoader.setController(MainController.CRUNCHER_CONTROLLER);
        Parent cruncherPane = fxmlLoader.load();
        gridPane.add(cruncherPane,1,0,1,3);

        primaryStage.setTitle("Word Distribution Tool");
        primaryStage.setScene(new Scene(gridPane, 1600, 900));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}