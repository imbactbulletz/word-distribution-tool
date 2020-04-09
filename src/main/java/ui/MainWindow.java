package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ui.controller.MainController;

public class MainWindow extends Application {

    public static final MainController MAIN_CONTROLLER = new MainController();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("xml/window_main_layout.fxml"));
        fxmlLoader.setController(MAIN_CONTROLLER);
        GridPane gridPane = fxmlLoader.load();

        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("xml/pane_input_layout.fxml"));
        fxmlLoader.setController(MainController.INPUT_CONTROLLER);
        Parent inputPane = fxmlLoader.load();
        MainController.INPUT_CONTROLLER.init();
        gridPane.add(inputPane, 0, 0, 1, 3);

        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("xml/pane_cruncher_layout.fxml"));
        fxmlLoader.setController(MainController.CRUNCHER_CONTROLLER);
        Parent cruncherPane = fxmlLoader.load();
        MainController.CRUNCHER_CONTROLLER.init();
        gridPane.add(cruncherPane, 1, 0, 1, 3);

        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("xml/pane_output.fxml"));
        Parent outputPane = fxmlLoader.load();
        gridPane.add(outputPane, 2, 0, 4, 3);

        primaryStage.setTitle("Word Distribution Tool");
        primaryStage.setScene(new Scene(gridPane, 1600, 900));
        primaryStage.setResizable(false);
        primaryStage.show();

        primaryStage.setOnCloseRequest((e) -> {
            MainController.terminateEverything();
            e.consume();
        });
    }
}