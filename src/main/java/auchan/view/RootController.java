package auchan.view;

import auchan.DBRobot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static auchan.DBRobot.isAdmin;
import static auchan.util.FX_utils.showAlert;

public class RootController {

    @FXML
    private BorderPane bp;

    @FXML
    private void initialize() {
        if (!isAdmin) {
            bp.setPrefWidth(200);
            bp.setMaxHeight(505);
        }
    }


    @FXML
    private void onClose() {
        Platform.exit();
    }

    @FXML
    private void onAbout() {
        String s = new BufferedReader(new InputStreamReader(DBRobot.class.getResourceAsStream("/About.txt"))).lines().collect(Collectors.joining("\n"));
        showAlert("DB2Excel About:", s, Alert.AlertType.INFORMATION);

    }
}
