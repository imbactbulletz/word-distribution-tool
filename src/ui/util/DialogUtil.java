package ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class DialogUtil {
    public static <T> Optional<T> showChoiceDialog(List<T> choices, String title, String headerText) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        return dialog.showAndWait();
    }

    public static void showErrorDialog(String title, String headerText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }

    public static File showDirectoryChooser(String path, Window parentWindow) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(Paths.get(path).toFile());
        return directoryChooser.showDialog(parentWindow);
    }
}
