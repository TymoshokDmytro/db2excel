package auchan;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.application.Application;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.Optional;

import static auchan.util.FX_utils.showAlert;

public class Test extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {

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
        rb1.setSelected(true);

        PasswordField pass = new PasswordField();
        pass.setDisable(true);

        rb1.setOnAction(event -> {
            pass.setDisable(true);
        });

        rb2.setOnAction(event -> {
            pass.setDisable(false);
            pass.requestFocus();
        });

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

        d.setResultConverter(new Callback<ButtonType, String>() {
            @Override
            public String call(ButtonType b) {
                if (b == buttonTypeOk) {
                    if (rb2.isSelected()) {
                        if (pass.getText().equals("1")) {
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
            }
        });

        Optional<String> result = d.showAndWait();

    }

    public static void main(String[] args) {
        launch(args);

    }

    private static void print(String s) {
        System.out.println(s);
    }


}
