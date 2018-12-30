package auchan.util;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static auchan.util.Log4J.Log;

public class FX_utils {

    public static ButtonType showAlertConf(String header, String content, Alert.AlertType at) {
        Alert alert = new Alert(at);
        alert.setTitle("DB2Excel");
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result =  alert.showAndWait();
        return result.get();
    }

    public static void showAlert(String header, String content, Alert.AlertType at) {
        Alert alert = new Alert(at);
        alert.setTitle("DB2Excel");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();

    }

    public static String getTextInputDialog(String profileName, String header, String content) {
        TextInputDialog dialog = new TextInputDialog(profileName);
        dialog.setTitle("DB2Excel");
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        Optional<String> result = dialog.showAndWait();
        return result.orElse("");
    }
    public static void showError(String header, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("DB2Excel");
        alert.setHeaderText(header);
        alert.setContentText("Stacktrace of error:");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String exceptionText = sw.toString();
        Log.error(header,ex);

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(false);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setContent(expContent);
        alert.setResizable(true);

        alert.showAndWait();
    }

    public static String userDialog(boolean isAdminChecked) {
        Dialog<String> d = new Dialog<>();
        d.setResizable(false);
        d.setTitle("DB2Excel");
        d.setHeaderText("Choose the user type:");

        ToggleGroup tg = new ToggleGroup();
        RadioButton rb1 = new RadioButton("User");
        RadioButton rb2 = new RadioButton("Administrator");
        RadioButton rb3 = new RadioButton();
        rb3.setVisible(false);
        rb3.setDisable(false);
        rb1.setToggleGroup(tg);
        rb2.setToggleGroup(tg);

        PasswordField pass = new PasswordField();

        rb1.setOnAction(event -> pass.setDisable(true));

        rb2.setOnAction(event -> {
            pass.setDisable(false);
            pass.requestFocus();
        });

        if (isAdminChecked) {
             rb2.setSelected(true);
             pass.setDisable(false);
             Platform.runLater(pass::requestFocus);
        } else {
            rb1.setSelected(true);
            pass.setDisable(true);
        }



        GridPane grid = new GridPane();
        grid.add(rb1, 0, 1);
        grid.add(rb2, 0, 2);
        grid.add(rb3, 0, 3);
        grid.add(pass, 0, 4);
        d.getDialogPane().setContent(grid);


        ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        d.getDialogPane().getButtonTypes().add(buttonTypeOk);
        d.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        d.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                if (rb2.isSelected()) {
                    if (pass.getText().equals("db2excel")) {
                        return "Admin";
                    } else {
                        showAlert("Password is incorrect", "", Alert.AlertType.ERROR);
                        return "Reopen";
                    }
                }
                if (rb1.isSelected()) {
                    return "User";
                }
            }
            return "Close";
        });

        Optional<String> result = d.showAndWait();
        return result.get();

    }

}
